package com.dcruzhe.esp32_blu

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputLayout
import java.util.UUID

const val REQUEST_ENABLE_BT = 1

class MainActivity : AppCompatActivity() {

    // --------- Bluetooth Variables ---------
    lateinit var mBluetoothAdapter: BluetoothAdapter            // Bluetooth Adapter
    var mAddressDevices: ArrayAdapter<String>? = null           // Address Devices
    var mNameDevices: ArrayAdapter<String>? = null              // Name Devices

    // --------- Set the UUID for the Bluetooth ---------
    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private var m_bluetoothSocket: BluetoothSocket? = null

        var m_isConnected: Boolean = false
        lateinit var m_address: String

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // --------- Initialize these Arrays ---------
        mAddressDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)


        // -------------------------- Get the Views --------------------------
        val btnActivateBluetooth = findViewById<MaterialButton>(R.id.btnActivateBluetooth)
        val btnDeactivateBluetooth = findViewById<MaterialButton>(R.id.btnDeactivateBluetooth)
        val btnScanDevices = findViewById<MaterialButton>(R.id.btnScan)
        val selectDevice = findViewById<AutoCompleteTextView>(R.id.selectDevice)
        val btnConnect = findViewById<MaterialButton>(R.id.btnConnect)


        val switchLabel1 = findViewById<MaterialSwitch>(R.id.switchLabel1)
        val switchLabel2 = findViewById<MaterialSwitch>(R.id.switchLabel2)
        val switchLabel3 = findViewById<MaterialSwitch>(R.id.switchLabel3)


        val inputSend = findViewById<TextInputLayout>(R.id.inputSend)
        val btnSend = findViewById<MaterialButton>(R.id.btnSend)


        val someActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == REQUEST_ENABLE_BT) {
                Log.i("MainActivity", "Bluetooth is activated")
            }
        }


        // --------- Initialize the Bluetooth Adapter ---------
        mBluetoothAdapter =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        // --------- Check if the Bluetooth is available ---------
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Bluetooth is available", Toast.LENGTH_LONG).show()
        }


        // ------------------------------ Initialize Buttons -----------------------------------

        // --------- Button Activate Bluetooth ---------
        btnActivateBluetooth.setOnClickListener {
            if (mBluetoothAdapter.isEnabled) {                 // If Bluetooth is already activated
                Toast.makeText(this, "Bluetooth is already activated", Toast.LENGTH_LONG).show()
                btnActivateBluetooth.text = getString(R.string.btn_activate_bluetooth)
            } else {
                val enableBluetoothIntent =
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)      // Activate Bluetooth
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT           // Check the permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("MainActivity", "ActivityCompat#requestPermissions")
                }
                someActivityResultLauncher.launch(enableBluetoothIntent)        // Launch the Intent
                btnActivateBluetooth.text = getString(R.string.btn_activate_bluetooth)


            }
        }

        // --------- Button Deactivate Bluetooth ---------
        btnDeactivateBluetooth.setOnClickListener {
            if (mBluetoothAdapter.isEnabled) {                 // If Bluetooth is already activated
                if (m_bluetoothSocket != null) {
                    try {
                        m_bluetoothSocket!!.close() // Close the Bluetooth socket
                        m_bluetoothSocket = null
                        m_isConnected = false
                        setEnableSwitches()
                        btnConnect.isEnabled = false
                        btnConnect.isEnabled = false
                        Toast.makeText(this, "Bluetooth connection is closed", Toast.LENGTH_LONG)
                            .show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "No active Bluetooth connection", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Bluetooth is already deactivated", Toast.LENGTH_LONG).show()
            }
            btnActivateBluetooth.text = getString(R.string.btn_no_activate_bluetooth)
        }


        // --------- Button Scan Devices ---------
        btnScanDevices.setOnClickListener {
            if (mBluetoothAdapter.isEnabled) {

                val pairedDevices: Set<BluetoothDevice>? =
                    mBluetoothAdapter.bondedDevices   // Get the paired devices
                mAddressDevices!!.clear()                                                   // Clear the Address Devices
                mNameDevices!!.clear()                                                      // Clear the Name Devices

                if (pairedDevices!!.isNotEmpty()) {                                        // If there are paired devices
                    for (device: BluetoothDevice in pairedDevices) {                      // Loop through the paired devices
                        mAddressDevices!!.add(device.address)                              // Add the MAC address of the device
                        mNameDevices!!.add(device.name)                                     // Add the name of the device
                    }
                    btnConnect.isEnabled = true
                }
                selectDevice.setAdapter(mNameDevices)                                      // Set the Adapter for the Name Devices

            } else {
                val noDevices = "No devices could be paired"
                mAddressDevices!!.add(noDevices)
                mNameDevices!!.add(noDevices)
                selectDevice.setAdapter(mNameDevices)
                btnConnect.isEnabled = false
                Toast.makeText(this, "First pair a device", Toast.LENGTH_LONG).show()
            }
        }


        // --------- Button Connect ---------
        btnConnect.setOnClickListener {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    val IntValueSelected = selectDevice.text.toString()
                    m_address =
                        mAddressDevices!!.getItem(mNameDevices!!.getPosition(IntValueSelected))
                            .toString()
                    Toast.makeText(this, "Connecting to $m_address", Toast.LENGTH_LONG).show()

                    mBluetoothAdapter.cancelDiscovery()       // Cancel the discovery
                    val device: BluetoothDevice =
                        mBluetoothAdapter.getRemoteDevice(m_address)              // Get the device
                    m_bluetoothSocket =
                        device.createInsecureRfcommSocketToServiceRecord(m_myUUID)          // Create a socket to connect to the device
                    m_bluetoothSocket!!.connect()                                                           // Connect to the device
                    m_isConnected = true
                    setEnableSwitches()
                }

                Toast.makeText(this, "Connected to $m_address", Toast.LENGTH_LONG).show()
                Log.i("MainActivity", "Connected")

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Connection failed", Toast.LENGTH_LONG).show()
                Log.i("MainActivity", "Connection failed")
                m_isConnected = false
                setEnableSwitches()
            }
        }

        // --------- Switch 1 ---------
        switchLabel1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendCommand("led1_on")
            } else {
                sendCommand("led1_off")
            }
        }

        // --------- Switch 2 ---------
        switchLabel2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendCommand("led2_on")
            } else {
                sendCommand("led2_off")
            }
        }

        // --------- Switch 3 ---------
        switchLabel3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendCommand("led3_on")
            } else {
                sendCommand("led3_off")
            }
        }


        // --------- Button Send ---------
        btnSend.setOnClickListener {
            if (inputSend.editText!!.text.isNotEmpty()) {
                val input = inputSend.editText!!.text.toString()
                sendCommand(input)
                Toast.makeText(this, "Command \"$input\" sent!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Please enter a command", Toast.LENGTH_LONG).show()
            }
            inputSend.editText!!.text.clear()
        }

    }


    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())         // Send the input to the device
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Please connect to a device", Toast.LENGTH_LONG).show()
        }
    }

    private fun setEnableSwitches() {
        findViewById<MaterialSwitch>(R.id.switchLabel1).isEnabled = m_isConnected
        findViewById<MaterialSwitch>(R.id.switchLabel2).isEnabled = m_isConnected
        findViewById<MaterialSwitch>(R.id.switchLabel3).isEnabled = m_isConnected
        findViewById<TextInputLayout>(R.id.inputSend).isEnabled = m_isConnected
        findViewById<MaterialButton>(R.id.btnSend).isEnabled = m_isConnected
    }

}