package com.sensor.andrew.reagen.andrew.hydrosense;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ERROR";
    // Bluetooth Variables
    private BluetoothAdapter thisBluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket btSocket = null;
    private InputStream inStream = null;

    // Bluetooth Consents
    int REQUEST_ENABLE_BT = 13;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String ADDR = "00:12:12:04:21:34";
    private String address;

    //Other Variables
    ArrayAdapter<String> mArrayAdapter;
    Button option, start;
    Intent optionI;

    //variables to delete later
    TextView testTv; //delete later
    TextView test2Tv;
    TextView test3Tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //When the program first starts connects to bluetooth
        getBluetoothAdapter();

        connect();


        //These are test Fields delete later
        testTv = (TextView) findViewById(R.id.test1TV); // delete later
        test2Tv = (TextView) findViewById(R.id.test2TV);
        test3Tv = (TextView) findViewById(R.id.test3TV);

        //Buttons should implement onclicklistenr later in time to clean up
        optionI = new Intent(this, OptionActivity.class);
        option = (Button) findViewById(R.id.optionBtn);
        option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(optionI);
            }
        });

        //start the bt
        start = (Button) findViewById(R.id.startBtn);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    try {
                        listenForData();
                    }catch(Exception eisl) {

                    }
            }
        });

    }
    // Check to see if phone has bluetooth if so check to see if bt is enabled
    private void getBluetoothAdapter() {
        thisBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (thisBluetoothAdapter == null) {
            //device does not support bluetooth display a message here
            Toast.makeText(getApplicationContext(),
                    "Your Device Doesn't Have Bluetooth", Toast.LENGTH_SHORT)
                    .show();
        } else {
            enableBluetooth();
        }

    }

    // If bluetooth is disabled: ask user to enable it then show already connected devices
    private void enableBluetooth() {
        if (!thisBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
       // connectedDevices();
    }

    // Show connected btDevices and get the address to be able to conncet to it
    private void connectedDevices() {
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        //check to see if there are any currently paired devices
        pairedDevices = thisBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            }

        }

        // creates a popup dialog that displays a list of connected devices, the user then chooses the bluetooth device
        // TOADD:: if there are no devices this should not display
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.list, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("BlueTooth Devices");
        ListView lv = (ListView) convertView.findViewById(R.id.lvDialog);
        lv.setAdapter(mArrayAdapter);

        final AlertDialog ad = alertDialog.show();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                address = mArrayAdapter.getItem(position);
                address = address.substring(address.lastIndexOf("\n") + 1);
                ad.dismiss();
            }
        });

    }

    //connect to the bluetooth device to get ready to share
    private void connect() {
        BluetoothDevice btDevice = thisBluetoothAdapter.getRemoteDevice(ADDR);
        thisBluetoothAdapter.cancelDiscovery();
        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            btSocket.connect();

            Toast.makeText(getApplicationContext(),
                    "connected to :" + btSocket.getRemoteDevice().getAddress(), Toast.LENGTH_SHORT)
                    .show();


        } catch (IOException io) {
            try {
                btSocket.close();
            } catch (IOException ioe) {
                // Unable to end the connection
            }
            Log.d(TAG, "socket failed");
            //socket creation failed
        }

    }

    private void listenForData() throws IOException {
        inStream = btSocket.getInputStream();
        final Handler handler = new Handler();
        Thread workerThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {

                        int bytesAvailable = inStream.available();

                        if (bytesAvailable > 0)
                        {

                            byte[] buffer = new byte[bytesAvailable];
                            inStream.read(buffer);
                            final String stringRec = new String(buffer, "UTF-8");
                            //time, sg, temp
                            final String[] stringSep = stringRec.split(",");


                            handler.post(new Runnable() {
                                public void run()
                                {
                                    testTv.setText(Integer.toString(stringSep.length));

                                    if(stringSep.length == 3) {
                                        String right = stringSep[0];
                                        test2Tv.setText(right);
                                        test3Tv.setText(stringSep[1]);
                                    }

                                }
                            });

                            //test2Tv.setText(stringSep[1]);
                           // test3Tv.setText(stringSep[2]);
                            
                        }
                    } catch (IOException iof) {
                        break;
                    }
                }
            }
        });
        workerThread.start();
    }
}
