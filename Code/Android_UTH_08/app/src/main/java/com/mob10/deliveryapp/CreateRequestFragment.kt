package com.mob10.deliveryapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class CreateRequestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_request, container, false)

        val etReceiverName = view.findViewById<EditText>(R.id.etReceiverName)
        val etAddress = view.findViewById<EditText>(R.id.etAddress)
        val etDistance = view.findViewById<EditText>(R.id.etDistance)
        val etWeight = view.findViewById<EditText>(R.id.etWeight)
        val btnCalculateFee = view.findViewById<Button>(R.id.btnCalculateFee)
        val tvFeeResult = view.findViewById<TextView>(R.id.tvFeeResult)
        val btnSubmitOrder = view.findViewById<Button>(R.id.btnSubmitOrder)

        btnCalculateFee.setOnClickListener {
            val distance = etDistance.text.toString().toDoubleOrNull() ?: 0.0
            val weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0

            if (distance > 0 && weight > 0) {
                val fee = FeeCalculator.calculateFee(distance, weight)
                tvFeeResult.text = "Tổng phí: ${String.format("%,.0f", fee)} VNĐ"
            } else {
                Toast.makeText(context, "Vui lòng nhập khoảng cách và khối lượng hợp lệ!", Toast.LENGTH_SHORT).show()
            }
        }

        btnSubmitOrder.setOnClickListener {
            val name = etReceiverName.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(context, "Vui lòng điền đầy đủ thông tin người nhận!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Tạo đơn hàng thành công!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}