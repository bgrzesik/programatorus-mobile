package programatorus.client.comm.app.proto

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.ContentResolverCompat
import com.google.protobuf.ByteString
import programatorus.client.comm.app.IRequester
import programatorus.client.comm.app.InvalidResponseException
import programatorus.client.comm.app.proto.FileUpload.Request.*
import programatorus.client.comm.session.ISession
import programus.proto.Protocol
import programus.proto.Protocol.FileUpload.FileType
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import programus.proto.Protocol.FileUpload as ProtoFileUpload

object FileUpload {
    private const val TAG = "FileUpload"

    fun upload(
        session: ISession,
        uri: Uri,
        contentResolver: ContentResolver
    ): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()

        val cursor = ContentResolverCompat.query(
            contentResolver, uri,
            null, null,
            null, null, null
        )

        if (cursor == null) {
            future.completeExceptionally(IOException("Unable to query file"))
            return future
        }

        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

        cursor.moveToFirst()

        val fileName = cursor.getString(nameIndex)
        val fileSize = cursor.getLong(sizeIndex).toInt()

        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            future.completeExceptionally(IOException("Unable to open file for reading"))
            return future
        }

        val path = uri.path
        if (path == null) {
            future.completeExceptionally(IOException("Invalid file"))
            return future
        }

        return upload(session, fileName, fileSize, inputStream)
    }

    fun upload(
        session: ISession,
        fileName: String,
        fileSize: Int,
        fileInputStream: InputStream
    ): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        handleState(future, session, State.Start(fileName, fileSize, fileInputStream))
        return future
    }

    private fun handleState(parent: CompletableFuture<Unit>, session: ISession, state: State) {
        Log.d(TAG, "handleState(): state=$state")
        val future = state.next(session)

        future.whenComplete { _, th ->
            th?.let { parent.completeExceptionally(it) }
        }

        future.thenApply {
            Log.d(TAG, "handleState(): state=$state -> state=$it")
            when (it) {
                is State.EndOfFile -> {
                    parent.complete(Unit)
                    return@thenApply
                }
                else -> handleState(parent, session, it)
            }
        }
    }


    private sealed class State {
        abstract fun next(session: ISession): CompletableFuture<State>

        data class Start(
            private val fileName: String,
            private val fileSize: Int,
            private val fileInputStream: InputStream,
        ) : State() {
            override fun next(session: ISession): CompletableFuture<State> {
                Log.d(TAG, "next(): state=Start fileName=$fileName, fileSize=$fileSize")
                return Start(fileName, fileSize, 0)
                    .request(session)
                    .thenApply {
                        Log.d(TAG, "next(): state=Start Start result=$it")
                        return@thenApply when (it.result) {
                            Result.OK -> Transferring(it.id, 0, fileInputStream)
                            else -> {
                                fileInputStream.close()
                                throw InvalidResponseException("${it.result}")
                            }
                        }
                    }
            }
        }

        data class Transferring(
            private var transferId: Long,
            private var partNo: Int,
            private val inputStream: InputStream
        ) : State() {

            private fun eof(session: ISession): CompletableFuture<State> =
                Finish(transferId, ByteArray(0))
                    .request(session)
                    .thenApply {
                        Log.d(TAG, "next(): state=Transferring Finish result=$it")
                        return@thenApply when (it.result) {
                            Result.OK -> EndOfFile
                            else -> {
                                inputStream.close()
                                throw InvalidResponseException("${it.result}")
                            }
                        }
                    }


            override fun next(session: ISession): CompletableFuture<State> {
                val bytes = ByteArray(2048)
                val read = inputStream.read(bytes)
                Log.d(TAG, "next(): state=Transferring transferId=$transferId read=$read")

                if (read == -1) {
                    Log.d(TAG, "next(): state=Transferring transferId=$transferId eof")
                    inputStream.close()
                    return eof(session)
                }

                return Part(transferId, partNo, bytes, read)
                    .request(session)
                    .thenApply {
                        Log.d(TAG, "next(): state=Transferring Part result=$it")
                        return@thenApply when (it.result) {
                            Result.OK -> Transferring(transferId, partNo + 1, inputStream)
                            Result.INVALID_CHECKSUM -> this@Transferring
                            else -> {
                                inputStream.close()
                                throw InvalidResponseException("${it.result}")
                            }
                        }
                    }
            }
        }

        object EndOfFile : State() {
            override fun next(session: ISession) =
                CompletableFuture<State>().apply { complete(this@EndOfFile) }
        }
    }

    private sealed class Request(private val mUid: Long?) : IRequester<Response> {
        abstract fun build(b: ProtoFileUpload.Builder): ProtoFileUpload.Builder

        override val responsePayloadCase get() = PayloadCase.FILEUPLOAD

        override fun prepareRequest() =
            GenericMessage.newBuilder()
                .setFileUpload(
                    build(ProtoFileUpload.newBuilder().apply {
                        mUid?.let { setUid(it) }
                    })
                )

        override fun handleResponse(message: GenericMessage) =
            Response(
                message.fileUpload.uid, when (message.fileUpload.result) {
                    ProtoFileUpload.Result.OK -> Result.OK
                    ProtoFileUpload.Result.INVALID_CHECKSUM -> Result.INVALID_CHECKSUM
                    ProtoFileUpload.Result.IO_ERROR -> Result.IO_ERROR
                    ProtoFileUpload.Result.ALREADY_EXISTS -> Result.ALREADY_EXISTS
                    else -> throw InvalidResponseException("Unrecognized status")
                }
            )

        class Start(
            val name: String,
            val size: Int,
            val chunks: Int,
        ) : Request(null) {
            override fun build(b: Protocol.FileUpload.Builder) = b.setStart(
                ProtoFileUpload.Start.newBuilder()
                    .setName(name)
                    .setSize(size.toLong())
                    .setChunks(chunks)
                    .setType(FileType.FIRMWARE)
            )
        }

        data class Part(val uid: Long, val partNo: Int, val chunk: ByteArray, val len: Int) :
            Request(uid) {
            override fun build(b: Protocol.FileUpload.Builder) = b.setPart(
                ProtoFileUpload.Part.newBuilder()
                    .setPartNo(partNo)
                    .setChunk(ByteString.copyFrom(chunk, 0, len))
            )
        }

        data class Finish(val uid: Long, val checksum: ByteArray) : Request(uid) {
            override fun build(b: Protocol.FileUpload.Builder) = b.setFinish(
                ProtoFileUpload.Finish.newBuilder()
                    .setChecksum(ByteString.copyFrom(checksum))
            )
        }

        enum class Result {
            OK,
            INVALID_CHECKSUM,
            IO_ERROR,
            ALREADY_EXISTS,
        }

        data class Response(val id: Long, val result: Result)
    }

}
