package com.example.ktxtravelapplication.planActivity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.recyclerview.widget.RecyclerView
import com.example.ktxtravelapplication.databinding.PlanDetailItemBinding
import com.example.ktxtravelapplication.planActivity.planRoomDB.PlanDB
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class TravelPlanRecyclerAdapter(val context: Context, val datas: MutableList<PlanDetailDatas>, val db: PlanDB) : RecyclerView.Adapter<TravelPlanRecyclerAdapter.ViewHolder>() {
    fun deletePlan(pos: Int) {
        if(returnState == "수정") {
            if(returnPos >= planPos) {
                runBlocking {
                    val id = db.getDao().getPlan(planNum).get(datas[pos].sequence - 1).id
                    db.getDao().updateDeleteState(id!!.toInt(), true)
                }
            }
        } else{ }
        datas.removeAt(pos)
        notifyItemRemoved(pos)

        planSeq = planSeq - 1
        planPos = planPos - 1
    }
    // 뷰 홀더 선언부
    inner class ViewHolder(val binding : PlanDetailItemBinding) : RecyclerView.ViewHolder(binding.root) {
        // 각 항목에서 작동하는 기능이나 텍스트값 등을 변경하는 함수
        fun bind(pos: Int){
            // TimePickerDialog 함수
            fun timePic(textNum: Int) {
                TimePickerDialog(itemView.context, object: TimePickerDialog.OnTimeSetListener{
                    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
                        val amOrPm: String
                        var hour = p1
                        val minute = p2
                        if(p1 >= 12 && p1 <= 24) {
                            amOrPm = "오후"
                            if(hour == 12) hour = hour
                            else hour = hour - 12
                        }  else{
                            amOrPm = "오전"
                        }
                        // textNum이 1일 경우 시작 시간을 변경, 2일 경우 끝나는 시간을 변경
                        if(textNum == 1) {
                            binding.planDetailTime.text = "${amOrPm} ${hour.toString()} : ${minute.toString().padStart(2, '0')}"
                            datas[pos].startTime = binding.planDetailTime.text.toString()
                        }
                        else {
                            binding.planDetailTime2.text = "${amOrPm} ${hour.toString()} : ${minute.toString().padStart(2, '0')}"
                            datas[pos].endTime = binding.planDetailTime2.text.toString()
                        }
                    }
                }, 15, 0, false).show()
            }

            // 초기값들 설정
            binding.planDetailNumber.text = "${pos + 1}번"
            binding.planDetailEditText.requestFocus()
            binding.planDetailEditText.setText(datas[pos].planDetail)
            binding.planSelectedDate.text = datas[pos].selectedDate
            binding.planDetailTime.text = datas[pos].startTime
            binding.planDetailTime2.text = datas[pos].endTime
            planPos = datas.size

            // 여행계획 시작 시간 클릭시 시간 변경창 띄우기
            binding.planDetailTime.setOnClickListener {
                timePic(1)
            }
            // 여행계획 끝나는 시간 클릭시 시간 변경창 띄우기
            binding.planDetailTime2.setOnClickListener {
                timePic(2)
            }

            // 계획 날짜 선택하기 버튼 클릭시
            binding.planDateBtn.setOnClickListener {
                // datePickerDialog를 띄워 날짜를 선택하도록 설정
                val datePickerDialog = DatePickerDialog(it.context, object: DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
                        // 버튼 옆에 텍스트를 선택한 날짜로 변경
                        binding.planSelectedDate.text = "$p1-${p2+1}-$p3"
                        datas[pos].selectedDate = binding.planSelectedDate.text.toString()
                    }
                }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(
                    Calendar.DAY_OF_MONTH))
                // 최소 최대 날짜 설정
                datePickerDialog.datePicker.minDate = minDate.timeInMillis
                datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
                datePickerDialog.show()
            }

            // 계획 텍스트 변경시
            binding.planDetailEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    datas[bindingAdapterPosition].planDetail = binding.planDetailEditText.text.toString()
                }
            })

            binding.planDeleteBtn.setOnClickListener {
                deletePlan(bindingAdapterPosition)
            }
        }
    }

    // 리사이클러뷰 항목 개수를 반환
    override fun getItemCount(): Int {
        return datas.size
    }

    // 뷰 홀더를 생성.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlanDetailItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    // 각 아이템 항목을 뷰 홀더에 맞춰서 출력
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }
}