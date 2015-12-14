package com.packruler.backlighthandler;


import android.os.AsyncTask;
import android.util.Log;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.nano.CodedOutputByteBufferNano;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import proto.nano.Hyperion.ClearRequest;
import proto.nano.Hyperion.HyperionReply;
import proto.nano.Hyperion.HyperionRequest;
import proto.nano.Hyperion.ImageRequest;

public class Hyperion {
    private static final String TAG = "Hyperion";
    private final Socket mSocket;
    private SendRequestAsync sendRequestAsync = new SendRequestAsync();

    public Hyperion(String address, int port) throws UnknownHostException, IOException {
        mSocket = new Socket(address, port);
    }

    @Override
    protected void finalize() throws Throwable {
        if (mSocket != null && mSocket.isConnected()) {
            mSocket.close();
        }
        super.finalize();
    }

    public void clear(int priority) throws IOException {
        ClearRequest clearRequest = new ClearRequest();
        clearRequest.priority = priority;

        HyperionRequest request = new HyperionRequest();
        request.command = HyperionRequest.CLEAR;
        request.setExtension(ClearRequest.clearRequest, clearRequest);

        sendRequest(request);
    }

    public void clearall() throws IOException {
        HyperionRequest request = new HyperionRequest();
        request.command = HyperionRequest.CLEARALL;

        sendRequest(request);
    }

    public void setImage(byte[] data, int width, int height, int priority) throws IOException {
        setImage(data, width, height, priority, -1);
    }

    public void setImage(byte[] data, int width, int height, int priority, int duration_ms) throws IOException {
        ImageRequest imageRequest = new ImageRequest();
        imageRequest.imagedata = data;
        imageRequest.imagewidth = width;
        imageRequest.imageheight = height;
        imageRequest.priority = priority;
        imageRequest.setDuration(duration_ms);

        HyperionRequest request = new HyperionRequest();
        request.command = HyperionRequest.IMAGE;
        request.setExtension(ImageRequest.imageRequest, imageRequest);

        sendRequest(request);
    }

    private void sendRequest(HyperionRequest request) throws IOException {
        sendRequestAsync.doInBackground(request);
    }

    private HyperionReply asyncRequest(HyperionRequest request) throws IOException {
        int size = request.getSerializedSize();

        // create the header
        byte[] header = new byte[4];
        header[0] = (byte) ((size >> 24) & 0xFF);
        header[1] = (byte) ((size >> 16) & 0xFF);
        header[2] = (byte) ((size >> 8) & 0xFF);
        header[3] = (byte) ((size) & 0xFF);

        // write the data to the socket
        OutputStream output = mSocket.getOutputStream();
        byte[] byteArray = new byte[size];
        CodedOutputByteBufferNano outputByteBufferNano = CodedOutputByteBufferNano.newInstance(byteArray);
        output.write(header);
        request.writeTo(outputByteBufferNano);
        CodedOutputStream stream = CodedOutputStream.newInstance(output);
        stream.writeRawBytes(byteArray);
        stream.flush();
        output.flush();

        return receiveReply();
    }

    private HyperionReply receiveReply() throws IOException {
//        Log.i(TAG, "Receive Reply");
//        InputStream input = mSocket.getInputStream();
//
//        byte[] header = new byte[4];
//        Log.i(TAG, "Header " + input.read(header, 0, 4));
//        int size = (header[0] << 24) | (header[1] << 16) | (header[2] << 8) | (header[3]);
//        byte[] data = new byte[size];
//        Log.i(TAG, "Part 2" + input.read(data, 0, size));
//
//        return HyperionReply.parseFrom(data);
        return null;
    }

    private class SendRequestAsync extends AsyncTask<HyperionRequest, Void, HyperionReply> {

        @Override
        protected HyperionReply doInBackground(HyperionRequest... params) {
            try {
                return asyncRequest(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HyperionReply hyperionReply) {
            if (hyperionReply.success)
                Log.i(TAG, "Success");
            else
                Log.i(TAG, hyperionReply.getError());
        }
    }
}
