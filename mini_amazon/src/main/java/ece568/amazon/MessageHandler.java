package ece568.amazon;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class MessageHandler {
    /**
     * send message to the server
     * 
     * @param builder
     * @param output
     * @return
     * @throws IOException
     */
    protected <T extends GeneratedMessageV3.Builder<?>> boolean sendMSG(T builder, OutputStream output)
            throws IOException {
        CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(output);
        codedOutputStream.writeUInt32NoTag(builder.build().toByteArray().length);
        builder.build().writeTo(codedOutputStream);
        codedOutputStream.flush();
        return true;
    }

    /**
     * receive message from the server
     * 
     * @param input
     * @return
     * @throws IOException
     */
    protected <T extends GeneratedMessageV3.Builder<?>> boolean recvMSG(T builder, InputStream input)
            throws IOException {
        CodedInputStream codedInputStream = CodedInputStream.newInstance(input);
        int length = codedInputStream.readRawVarint32();
        int parseLimit = codedInputStream.pushLimit(length);
        builder.mergeFrom(codedInputStream);
        codedInputStream.popLimit(parseLimit);
        return true;
    }
}
