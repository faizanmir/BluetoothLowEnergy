package avishkaar.com.makerboardplusbluetooth

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BluetoothListAdapter(
    var bluetoothArrayList: ArrayList<BluetoothDevice?>,
    var mListerner: OnDeviceClickListener
) : RecyclerView.Adapter<BluetoothListAdapter.BluetoothViewHolder>() {
    interface OnDeviceClickListener {
        fun onDeviceSelected(bluetoothDevice: BluetoothDevice?)
    }


    inner class BluetoothViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        var deviceName: TextView = itemView.findViewById(R.id.deviceName)
        override fun onClick(p0: View?) {
            mListerner.onDeviceSelected(bluetoothArrayList[adapterPosition])
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothViewHolder {
        return BluetoothViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_bluetooth,
                parent,
                false
            )
        )
    }


    override fun getItemCount(): Int {
        return bluetoothArrayList.size
    }


    override fun onBindViewHolder(holder: BluetoothViewHolder, position: Int) {
        holder.deviceName.text = bluetoothArrayList[position]?.name


    }
}