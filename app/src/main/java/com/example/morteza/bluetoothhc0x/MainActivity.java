package com.example.morteza.bluetoothhc0x;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class MainActivity extends Activity  {

    private BluetoothAdapter BA;
    private Set<BluetoothDevice>pairedDevices;
    public final String TAG = "Main";
    byte endline;

    private Bluetooth bt;
    FloatingActionButton fab;
    TextView txtStatus ;
    EditText editTxtSubmit;
    EditText editTxtReceive;
    Button btnSubmit;
    Button btnErase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Setup();
        timers();
        addListenerOnButton();
    }

    private void addListenerOnButton() {

        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.chkbox_r:
                        endline=2;
                        break;
                    case R.id.chkbox_n:
                        endline=1;
                        break;
                    case R.id.chkbox_rn:
                        endline=3;
                        break;
                    case R.id.chkbox_no:
                        endline=0;
                        break;
                }
            }
        });
    }




    @Override
    protected void onPause() {
        super.onPause();
        bt.stop();
    }

    private void timers() {

        new Thread() {
            @Override
            public void run() {

                while (true){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // do
                            switch (bt.getState())
                            {
                                /*case 0:
                                    fab.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                                    break;*/
                                case 1:
                                    fab.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                                    txtStatus.setText(R.string.txt_disconnect);
                                    break;
                                case 2:
                                    fab.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
                                    txtStatus.setText(R.string.txt_connecting);
                                    break;
                                case 3:
                                    fab.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                                    txtStatus.setText(R.string.txt_connected);
                                    break;

                            }


                        }
                    });
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void Setup() {
        BA = BluetoothAdapter.getDefaultAdapter();
        bt = new Bluetooth(this, mHandler);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        txtStatus= (TextView)findViewById(R.id.txtStatus);
        editTxtSubmit=(EditText)findViewById(R.id.editTxtSubmit);
        editTxtReceive=(EditText)findViewById(R.id.editTxtReceive);
        btnErase=(Button)findViewById(R.id.btnErase);
        btnSubmit=(Button)findViewById(R.id.btnSubmit);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*               Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                ShowAlertDialogWithListview();
            }
        });


        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            //Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
        } else {
            // Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }

    }



    public void btnSubmit(View v){


        if(bt.getState()==1 || bt.getState()==2 || bt.getState()==0 ) {

            Snackbar.make(v, R.string.txt_disconnect, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }else{
            Snackbar.make(v, "Send", Snackbar.LENGTH_LONG)
                     .setAction("Action", null).show();
            switch(endline) {
                case 0:
                    bt.sendMessage(editTxtSubmit.getText().toString());
                    break;
                case 1:
                    bt.sendMessage(editTxtSubmit.getText().toString()+"\r");
                    break;
                case 2:
                    bt.sendMessage(editTxtSubmit.getText().toString()+"\n");
                    break;
                case 3:
                    bt.sendMessage(editTxtSubmit.getText().toString()+"\r\n");
                    break;
            }


        }
    }

    public void btnErase(View v){
          editTxtReceive.setText("");
    }




    public void ShowAlertDialogWithListview()
    {

        List<String> listDevices = new ArrayList<String>();
        pairedDevices = BA.getBondedDevices();
        for(BluetoothDevice bt : pairedDevices) listDevices.add(bt.getName());

        //Create sequence of items
        final CharSequence[] Devices = listDevices.toArray(new String[listDevices.size()]);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Devices connected:");
        dialogBuilder.setItems(Devices, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String selectedText = Devices[item].toString();  //Selected item in listview

                connectService(selectedText);

                Log.e("STATE_CONNECTED", "U"+Bluetooth.MESSAGE_STATE_CHANGE);

            }
        });
        //Create alert dialog object via builder
        AlertDialog alertDialogObject = dialogBuilder.create();
        //Show the dialog
        alertDialogObject.show();
    }




    public void connectService(String NameDivec){
        try {
            //    status.setText("Connecting...");
            Toast.makeText(getApplicationContext(),"Connecting "+NameDivec ,Toast.LENGTH_LONG).show();
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bt.start();
                bt.connectDevice(NameDivec);
                Log.d(TAG, "Btservice started - listening");
                //   status.setText("Connected");

            } else {
                Log.w(TAG, "Btservice started - bluetooth is not enabled");
                //   status.setText("Bluetooth Not enabled");
            }
        } catch(Exception e){
            Log.e(TAG, "Unable to start bt ",e);
            //   status.setText("Unable to connect " +e);

        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case Bluetooth.MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE ");
                    break;
                case Bluetooth.MESSAGE_READ:
                    Log.d(TAG, "MESSAGE_READ " + bt.InputStreamString());
                    editTxtReceive.setText(bt.InputStreamString());
                    break;
                case Bluetooth.MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MESSAGE_DEVICE_NAME " + msg);
                    break;
                case Bluetooth.MESSAGE_TOAST:
                    Log.d(TAG, "MESSAGE_TOAST " + msg);
                    break;
            }
        }
    };





}
