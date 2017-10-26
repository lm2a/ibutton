package com.indra.procesos;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.indra.procesos.usb.IUsbConnectionHandler;
import com.indra.procesos.usb.Logger;
import com.indra.procesos.usb.UsbDS9490RController;
import com.indra.procesos.usb.Util;

import java.util.List;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    private static final int VID = 0x04fa; //Vendor ID
    private static final int PID = 0x2490; //Product ID, este creo que es el bridge chip para enlazar el telefono con el IButton
    private static UsbDS9490RController sUsbController;
    private TextView pageContent, scratchpadContent;
    private Spinner romIDSpinner;

    String[] rom_ids;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); // XML file for application layout
        if(sUsbController == null){
            sUsbController = new UsbDS9490RController(this, mConnectionHandler, VID, PID);
        }
        ((Button)findViewById(R.id.buttonEnumerate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sUsbController == null)
                    sUsbController = new UsbDS9490RController(MainActivity.this, mConnectionHandler, VID, PID);
                else{
                    sUsbController.stop();
                    sUsbController = new UsbDS9490RController(MainActivity.this, mConnectionHandler, VID, PID);
                }

                pageContent = (TextView) findViewById(R.id.pageContent);
                scratchpadContent = (TextView) findViewById(R.id.scartchpadContent);
                romIDSpinner = (Spinner) findViewById(R.id.romIDSpinner);

                // Start a new Async Thread for initializing the USB and searching for
                // all ROM codes
                new InitUsbEpTask().execute();
                new GetAddressTask().execute();

            }
        });

        ((Spinner)findViewById(R.id.romIDSpinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // Start a new AsyncTask Thread for getting the real time temperature
                new GetMemoryTask().execute(position);
                new WriteScratchpadTask().execute(position);
                new GetScratchpadTask().execute(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private final IUsbConnectionHandler mConnectionHandler = new IUsbConnectionHandler() {
        @Override
        public void onUsbStopped() {
            Logger.e("Usb stopped!");
        }

        @Override
        public void onErrorLooperRunningAlready() {
            Logger.e("Looper already running!");
        }

        @Override
        public void onDeviceNotFound() {
            if(sUsbController != null){
                sUsbController.stop();
                sUsbController = null;
            }
        }
    };

    // AsyncTask for initializing the USB DS2480.
    private class InitUsbEpTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            sUsbController.initUsbDevice();
            return null;
        }
    }

    // AsyncTask for getting all the rom id of devices on the 1-Wire bus
    private class GetAddressTask extends AsyncTask<byte[], Void, byte[][]> {

        @Override
        protected byte[][] doInBackground(byte[]... romid) {
            sUsbController.getAddresses();
            return sUsbController.ROM_NO_LIST;
        }

        protected void onPostExecute(byte[][] result) {
            ArrayAdapter<String> spinnerArrayAdapter;
            rom_ids = new String[sUsbController.deviceCount];

            StringBuilder sb;

            for(int i = 0; i < rom_ids.length; i++) {
                sb = new StringBuilder();
                for(byte b: result[i]) {
                    sb.append(String.format("%02x ", b & 0xff));
                }

                rom_ids[i] = sb.toString();
            }

            spinnerArrayAdapter = new ArrayAdapter<String> (getApplicationContext(),
                    R.layout.spinner_style, rom_ids);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);

            romIDSpinner.setAdapter(spinnerArrayAdapter);

        }
    }

    // AsyncTask Thread for getting page data
    private class GetMemoryTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... romidIdx) {
            byte[] data = sUsbController.getMemoryData(sUsbController.ROM_NO_LIST[romidIdx[0]], 3, 32);
            Logger.e("Page data= "+Util.bytesToHex(data));
            return Util.bytesToHex(data).substring(4);
        }

        protected void onPostExecute(String result) {
            List<String> bytes = Util.splitEqually(result, 8);
            pageContent.setText("Memory Data: " + bytes.toString());
        }

    }

    // AsyncTask to write data on scratchpad
    private class WriteScratchpadTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... romidIdx) {
            byte[] data = new byte[]{(byte)0x00, (byte)0xBE, (byte)0xBA, (byte)0x00, (byte)0xCA, (byte)0xFE};
            Logger.e(Util.bytesAsHexa(data));
            sUsbController.saveDataOnScratchpad(sUsbController.ROM_NO_LIST[romidIdx[0]], data);
            return null;
        }

        protected void onPostExecute(Void result) {
            Logger.e("Data saved in scratchpad");
        }

    }

    // AsyncTask Thread for getting the real time temperature
    private class GetScratchpadTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... romidIdx) {
            byte[] dataScratch = sUsbController.getScratchpadData(sUsbController.ROM_NO_LIST[romidIdx[0]]);
            Logger.e("dataScratch= "+Util.bytesToHex(dataScratch));
            return Util.bytesToHex(dataScratch).substring(4);
        }

        protected void onPostExecute(String result) {
            List<String> bytes = Util.splitEqually(result, 8);
            scratchpadContent.setText("Scratchpad content: " + bytes.toString());
        }

    }


}