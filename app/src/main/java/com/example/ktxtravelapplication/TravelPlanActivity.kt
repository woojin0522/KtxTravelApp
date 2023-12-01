package com.example.ktxtravelapplication

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.ktxtravelapplication.databinding.ActivityTravelPlanBinding
import com.example.ktxtravelapplication.databinding.PlanDetailItemBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar

// --------------------------------전역 변수 영역-------------------------
// 계획 날짜 최소, 최대 범위 설정 변수
val minDate = Calendar.getInstance()
val maxDate = Calendar.getInstance()
var planPos = 0
var planNum = 0
var returnPos = 0
var returnState = ""
var planSeq = 1
// --------------------------------전역 변수 영역-------------------------
class TravelPlanActivity : AppCompatActivity() {
    // 변수 선언 영역 ------------------------
    lateinit var planTitle: String
    lateinit var planStartDate: String
    lateinit var planEndDate: String
    lateinit var binding: ActivityTravelPlanBinding
    lateinit var db: PlanDB

    // ------------------------ 변수 선언 영역
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩
        binding = ActivityTravelPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액션바 대신 툴바 사용 및 타이틀 비워 두기
        setSupportActionBar(binding.planToolbar)
        supportActionBar?.setTitle("")

        // DB 객체 선언
        val db = Room.databaseBuilder(
            applicationContext,
            PlanDB::class.java,
            "PlanDB"
        ).build()

        // ----------------------------------캘린더 영역 -----------------------------------
        // 현재 날짜를 초기화. 안드로이드 8 버전 이상부터 사용
        binding.planStartCalendarDay.text = LocalDate.now().toString() + " ~ "
        binding.planEndCalendarDay.text = LocalDate.now().toString()
        binding.planDayRange.text = "당일치기"
        // 캘린더뷰 설정
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        binding.calendarView.state().edit()
            .setMinimumDate(CalendarDay.from(currentYear, currentMonth, currentDay))
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()
        // 캘린더뷰 날짜 범위 선택시
        binding.calendarView.setOnRangeSelectedListener { widget, dates ->
            binding.planStartCalendarDay.text =
                "${dates[0].year}-${dates[0].month}-${dates[0].day} ~ "
            binding.planEndCalendarDay.text =
                "${dates[dates.size - 1].year}-${dates[dates.size - 1].month}-${dates[dates.size - 1].day}"
            binding.planDayRange.text = "${dates.size - 1}박${dates.size}일"
            // 최소날짜와 최대날짜를 여행 날짜 선택 범위 최소와 최대값으로 설정
            minDate.set(dates[0].year, dates[0].month - 1, dates[0].day)
            maxDate.set(
                dates[dates.size - 1].year,
                dates[dates.size - 1].month - 1,
                dates[dates.size - 1].day
            )
        }
        // 캘린더뷰 하루만 선택시
        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            binding.planStartCalendarDay.text = "${date.year}-${date.month}-${date.day} ~ "
            binding.planEndCalendarDay.text = "${date.year}-${date.month}-${date.day}"
            binding.planDayRange.text = "당일치기"
            // 최소날짜와 최대날짜를 여행 날짜 선택 범위 최소와 최대값으로 설정
            minDate.set(date.year, date.month - 1, date.day)
            maxDate.set(date.year, date.month - 1, date.day)
        }
        // ----------------------------------캘린더 영역 -----------------------------------

        // 여행계획 데이터
        val datas = mutableListOf<PlanDetailDatas>()

        // ----------------------------------값 전달받는 영역--------------------------------
        val returnPlanTitle = intent.getStringExtra("returnTitle")
        val returnPlanStartDate = intent.getStringExtra("returnStartDate")
        val returnPlanEndDate = intent.getStringExtra("returnEndDate")
        returnState = intent.getStringExtra("returnState").toString()
        val returnPlanNumber = intent.getIntExtra("returnPlanNumber", 0)
        returnPos = intent.getIntExtra("returnPos", 0)

        // 전달받은 값이 null일 경우 아무 동작도 취하지 않음
        planNum = returnPlanNumber
        if (returnPlanTitle == null || returnPlanStartDate == null || returnPlanEndDate == null) {
        } else {
            // 전달받은 값을 각 화면에 출력
            binding.planTitle.setText(returnPlanTitle)
            binding.planStartCalendarDay.text = returnPlanStartDate
            binding.planEndCalendarDay.text = returnPlanEndDate

            // 몇박몇일 인지 계산
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val startDate = dateFormat.parse(returnPlanStartDate).time
            val endDate = dateFormat.parse(returnPlanEndDate).time
            val subDate = (endDate - startDate) / (24 * 60 * 60 * 1000)

            if (subDate.toInt() == 0) {
                binding.planDayRange.text = "당일치기"
            } else {
                binding.planDayRange.text = "${subDate}박${subDate + 1}일"
            }
        }
        // ----------------------------------값 전달받는 영역--------------------------------

        // 수정모드 일때 버튼 이름을 수정하기로 바꾸기
        if (returnState == "수정") {
            binding.planSaveBtn.text = "수정하기"

            // returnPos - 1 수 만큼 빈 데이터 add후 db에서 select해서 returnPos - 1 수 만큼 데이터 수정
            for (i in 0..returnPos - 1) {
                datas.add(PlanDetailDatas(i + 1, "", "오후 12 : 00", "오후 1 : 00", ""))
            }
            runBlocking {
                for (i in 0..returnPos - 1) {
                    val SelectedDate = db.getDao().getPlan(planNum).get(i).SelectedDate
                    val StartTime = db.getDao().getPlan(planNum).get(i).StartTime
                    val EndTime = db.getDao().getPlan(planNum).get(i).EndTime
                    val DetailText = db.getDao().getPlan(planNum).get(i).Detail
                    datas.set(
                        i,
                        PlanDetailDatas(i + 1, SelectedDate, StartTime, EndTime, DetailText)
                    )

                    planSeq = returnPos
                    binding.planDetailRecyclerView.adapter?.notifyItemChanged(i)
                }
            }
        } else {
            datas.add(PlanDetailDatas(planSeq, "", "오후 12 : 00", "오후 1 : 00", ""))
        }

        // -----------------------------------함수 영역 ------------------------------------
        // 제목 값이 비어 있을 경우 경고창 표시
        fun titleEmpty(myString: String) {
            AlertDialog.Builder(this).run {
                setTitle("경고")
                setMessage("${myString}을 입력 해주세요.")
                setPositiveButton("확인", null)
                show()
            }
        }

        // 값 저장 및 인텐트 값 넘겨주기 함수
        fun planSave(state: String) {
            // 제목 날짜 저장
            planTitle = binding.planTitle.text.toString()
            planStartDate = binding.planStartCalendarDay.text.toString()
            planEndDate = binding.planEndCalendarDay.text.toString()
            // 저장(신규등록) 모드일 때
            if (returnState == "저장") {
                // insert
                runBlocking {
                    if (planPos >= 1) {
                        for (i in 0..planPos - 1) {
                            db.getDao().insertPlan(
                                PlanEntity(
                                    null,
                                    false,
                                    planNum,
                                    planTitle,
                                    planStartDate,
                                    planEndDate,
                                    datas[i].selectedDate,
                                    datas[i].startTime,
                                    datas[i].endTime,
                                    datas[i].planDetail
                                )
                            )
                        }
                    }
                }
            }
            // 수정 모드일 때
            else {
                /*returnPos가 planPos보다 작을 경우에 실행. 이 때는 DB에 있는 값에서 새로운 항목이 추가되는 것이므로,
                DB에 새로 추가된 항목을 insert해주어야 한다.*/
                // insert
                if (returnPos < planPos) {
                    runBlocking {
                        for (i in returnPos..planPos - 1) {
                            db.getDao().insertPlan(
                                PlanEntity(
                                    null,
                                    false,
                                    planNum,
                                    planTitle,
                                    planStartDate,
                                    planEndDate,
                                    datas[i].selectedDate,
                                    datas[i].startTime,
                                    datas[i].endTime,
                                    datas[i].planDetail
                                )
                            )
                        }
                    }

                    runBlocking {
                        val endIndex = db.getDao().getPlan(planNum).size
                        if (endIndex != 0) {
                            for (i in 0..endIndex - 1) {
                                if (db.getDao().getPlan(planNum).get(i).deleteState == false) {
                                    val id = db.getDao().getPlan(planNum).get(i).id
                                    db.getDao().updatePlan(
                                        id!!.toInt(),
                                        planTitle,
                                        datas[i].planDetail,
                                        planStartDate,
                                        planEndDate,
                                        datas[i].startTime,
                                        datas[i].endTime,
                                        datas[i].selectedDate
                                    )
                                }
                            }
                        } else {
                        }
                    }
                }
                // returnPos가 planPos보다 크거나 같을 경우에 실행. 이때는 새로이 추가되는 항목이 없으므로 insert할 필요가 없음.
                else {
                    // 삭제기능
                    runBlocking {
                        var whileStop = false
                        while (true) {
                            // for문을 통해 deleteState 검출, true이면 db 삭제후 for문 빠져나가기, true검출 안되면 whileStop = true
                            for (i in 0..db.getDao().getPlan(planNum).size - 1) {
                                val delState = db.getDao().getPlan(planNum).get(i).deleteState
                                if (delState == true) {
                                    db.getDao().deletePlan(true)
                                    break
                                } else {
                                    whileStop = true
                                }
                            }
                            // whileStop 이 true일 때 while문 빠져나가기
                            if (whileStop == true || db.getDao().getPlan(planNum).size == 0) {
                                break
                            }
                        }
                    }

                    runBlocking {
                        val startIndex = db.getDao().getPlan(planNum).size
                        for (i in startIndex..planPos - 1) {
                            db.getDao().insertPlan(
                                PlanEntity(
                                    null,
                                    false,
                                    planNum,
                                    planTitle,
                                    planStartDate,
                                    planEndDate,
                                    datas[i].selectedDate,
                                    datas[i].startTime,
                                    datas[i].endTime,
                                    datas[i].planDetail
                                )
                            )
                        }
                    }
                    runBlocking {
                        val endIndex = db.getDao().getPlan(planNum).size
                        if (endIndex != 0) {
                            for (i in 0..endIndex - 1) {
                                if (db.getDao().getPlan(planNum).get(i).deleteState == false) {
                                    val id = db.getDao().getPlan(planNum).get(i).id
                                    db.getDao().updatePlan(
                                        id!!.toInt(),
                                        planTitle,
                                        datas[i].planDetail,
                                        planStartDate,
                                        planEndDate,
                                        datas[i].startTime,
                                        datas[i].endTime,
                                        datas[i].selectedDate
                                    )
                                }
                            }
                        } else {
                        }
                    }
                }
            }

            // 인텐트 생성후 인텐트로 데이터 넘기기
            val returnIntent = Intent()
            returnIntent.putExtra("returnTitle", planTitle)
            returnIntent.putExtra("returnStartDate", planStartDate)
            returnIntent.putExtra("returnEndDate", planEndDate)
            returnIntent.putExtra("returnState", state)
            returnIntent.putExtra("returnPlanNumber", returnPlanNumber)
            returnIntent.putExtra("returnPos", planPos)
            setResult(Activity.RESULT_OK, returnIntent)
            // 이전화면으로 값 넘겨주기
            finish()
        }

        // 뒤로가기 버튼 기능 함수
        fun backBtn(state: String) {
            AlertDialog.Builder(this).run {
                val eventHandler = object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        if (p1 == DialogInterface.BUTTON_POSITIVE) {
                            // 제목값 검사
                            planTitle = binding.planTitle.text.toString()
                            // 제목값 비어있을 경우 경고창 표시
                            if (planTitle == "") titleEmpty("제목")
                            // 제목값 있을 경우 저장
                            else planSave(state)
                        } else {
                            finish()
                        }
                    }
                }

                setTitle("나가기")
                /*                setIcon(android.R.drawable.ic_dialog_info)*/
                setMessage("나가기 전에 ${state}하시겠습니까?")
                setPositiveButton("네", eventHandler)
                setNegativeButton("아니오", eventHandler)
                show()
            }
        }

        // 저장하기 버튼 기능 함수
        fun saveBtn(state: String) {
            AlertDialog.Builder(this).run {
                val eventHandler = object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        if (p1 == DialogInterface.BUTTON_POSITIVE) {
                            planSave(state)
                        } else {
                        }
                    }
                }

                setTitle("저장여부")
                /*                setIcon(android.R.drawable.ic_dialog_info)*/
                setMessage("정말 ${state}하시겠습니까?")
                setPositiveButton("네", eventHandler)
                setNegativeButton("아니오", eventHandler)
                show()
            }
        }
        // -----------------------------------함수 영역 ------------------------------------

        // -----------------------------------뒤로가기 기능 ---------------------------------
        // 상단바 뒤로가기 버튼
        binding.planBackBtn.setOnClickListener {
            if (returnState == "수정") {
                backBtn("수정")
            } else {
                backBtn("저장")
            }
        }

        // 뒤로가기 버튼 클릭시
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (returnState == "수정") {
                    backBtn("수정")
                } else {
                    backBtn("저장")
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback = callback)
        // -----------------------------------뒤로가기 기능 ---------------------------------

        // -----------------------------------버튼 작동 영역 --------------------------------
        // 시간별 계획 추가 버튼 클릭시
        binding.planDetailPlusBtn.setOnClickListener {
            planSeq = planSeq + 1
            datas.add(PlanDetailDatas(planSeq, "", "오후 12 : 00", "오후 1 : 00", ""))
            binding.planDetailRecyclerView.adapter?.notifyItemInserted(datas.size)
        }
        // 시간별 계획 저장 버튼 클릭시
        binding.planSaveBtn.setOnClickListener {
            if (returnState == "수정") {
                // 수정 버튼 클릭시 확인 여부창 띄우기
                planTitle = binding.planTitle.text.toString()
                if (planTitle == "") titleEmpty("제목")
                else saveBtn("수정")
            } else {
                // 저장 버튼 클릭시 확인 여부창 띄우기
                planTitle = binding.planTitle.text.toString()
                if (planTitle == "") titleEmpty("제목")
                else saveBtn("저장")
            }
        }
        // 여행 날짜 선택하기버튼 클릭시
        binding.planCalanderBtn.setOnClickListener {
            // 캘린더뷰가 숨겨져있을경우 캘린더뷰를 띄우고 버튼 텍스트를 날짜 선택완료로 변경
            if (binding.calendarView.visibility == View.GONE) {
                binding.calendarView.visibility = View.VISIBLE
                binding.planCalanderBtn.text = "날싸 선택완료"

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(window.decorView.applicationWindowToken, 0)
            } else {
                // 캘린터뷰가 화면에 보일경우 캘린더뷰를 숨기고 버튼 텍스트를 날짜 선택하기로 변경
                binding.calendarView.visibility = View.GONE
                binding.planCalanderBtn.text = "날짜 선택하기"
            }
        }
        // -----------------------------------버튼 작동 영역 --------------------------------

        // 리사이클러뷰 어댑터와 레이아웃 매니저 설정
        binding.planDetailRecyclerView.adapter = TravelPlanRecyclerAdapter(this, datas, db)
        binding.planDetailRecyclerView.layoutManager = LinearLayoutManager(this)

    }

    // 화면 전환간 애니메이션 제거
    override fun onPause() {
        super.onPause()
        // 만약 api34일 경우 overrideActivityTransition 사용 추가 해야함.
        overridePendingTransition(0,0)
    }
}

// -------------------------------------여행계획 데이터 클래스-------------------------------------
data class PlanDetailDatas(
    var sequence: Int,
    var selectedDate: String,
    var startTime: String,
    var endTime: String,
    var planDetail: String,
)

// --------------------------------시간별 계획 리사이클러뷰 어댑터----------------------------------
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
                }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
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