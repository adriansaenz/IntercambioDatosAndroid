package com.example.adrian.intercambioarchivos;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.*;

public class MainActivity extends AppCompatActivity {
    private TextView labelIP, labelEstado, labelArchivo, textoEstado, textoArchivo;
    private EditText textoIP;
    private InetAddress ip;
    private File elemento;
    int request_code = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        labelIP = (TextView) findViewById(R.id.labelIP); textoIP = (EditText) findViewById(R.id.textoIP);
        labelEstado = (TextView) findViewById(R.id.labelEstado); textoEstado = (TextView) findViewById(R.id.textoEstado);
        labelArchivo = (TextView) findViewById(R.id.labelArchivo); textoArchivo = (TextView) findViewById(R.id.textoArchivo);
    }



    public void onClickElegir(View v){
        Intent ListSong = new Intent(getApplicationContext(),PoIExplorer.class);
        startActivityForResult(ListSong,request_code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if ((requestCode == request_code) && (resultCode == RESULT_OK)){
            Toast.makeText(getApplicationContext(),data.getDataString(),Toast.LENGTH_LONG).show();
            elemento = new File(data.getDataString());
        }
    }

    public void onClickEnviar(View v){
        MyClientTask myClientTask = new MyClientTask(
                textoIP.getText().toString(),
                5005);
        myClientTask.execute();
    }

    public class MyClientTask extends AsyncTask<Void, Integer, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        MyClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            sendFile(dstAddress);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textoEstado.setText(response);
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progreso = values[0].intValue();
            switch (progreso){
                case 0:
                    textoEstado.setText("Enviando...");
                    break;
                case 1:
                    textoEstado.setText("Enviado.");
                    break;
                case 2:
                    textoEstado.setText(response);
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(),"Seleccione primero el archivo a enviar", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        public String sendFile(String dstAddress){

            if(elemento != null){
                DataInputStream input;
                BufferedInputStream bis;
                BufferedOutputStream bos;
                int in;
                byte[] byteArray;
                dstAddress = dstAddress.replace(".", "-");
                String[] ips = dstAddress.split("-");

                try {
                    byte[] bIp = new byte[4];

                    bIp[0] = Short.valueOf(ips[0]).byteValue();
                    bIp[1] = Short.valueOf(ips[1]).byteValue();
                    bIp[2] = Short.valueOf(ips[2]).byteValue();
                    bIp[3] = Short.valueOf(ips[3]).byteValue();
                    Socket cliente = new Socket(InetAddress.getByAddress(bIp),5005);
                    bis = new BufferedInputStream(new FileInputStream(elemento));
                    bos = new BufferedOutputStream(cliente.getOutputStream());
                    input = new DataInputStream(cliente.getInputStream());
                    DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());
                    dos.writeUTF(elemento.getName());
                    byteArray = new byte[8192];
                    publishProgress(0);
                    while ((in = bis.read(byteArray)) != -1) bos.write(byteArray,0,in);
                    publishProgress(1);
                    bis.close();
                    bos.close();
                    dos.close();
                    response = input.readUTF();
                    publishProgress(2);
                    cliente.close();
                }catch(Exception ex){
                    Log.v("Error",ex.getMessage());}
            }
            else publishProgress(3);

            return response;
        }
    }
}
