package com.example.ktxtravelapplication.mapActivity.tourData

//한국관광정보 api에서 데이터 불러오기 -> json 혹은 xml로 이루어진 데이터를 파싱해서 변환 -> 마커 등록 -> 클러스터링 하기
data class TourData (
    val title: String, // 관광지 이름
    val addr1: String, // 관광지 주소 1
    val addr2: String?, // 관광지 주소 2
    val imageUri: String?, // 관광지 이미지 uri
    val dist: Double, // 역에서 관광지까지의 거리
    val latitude: Double, // 관광지 y좌표
    val longitude: Double, // 관광지 x좌표
    val infomation: String, // 관광지 정보
    val tel: String, // 관광지 전화번호
    val likeCount: Int // 좋ㅇ요 수
)