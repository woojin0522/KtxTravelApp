package com.example.ktxtravelapplication.mapActivity.tourData

data class TourData (
    val title: String, // 관광지 이름
    val addr1: String, // 관광지 주소 1
    val addr2: String?, // 관광지 주소 2
    val imageUri: String?, // 관광지 이미지 uri
    val dist: Double, // 역에서 관광지까지의 거리
    val latitude: Double, // 관광지 y좌표
    val longitude: Double, // 관광지 x좌표
    var infomation: String, // 관광지 정보
    var homepageUrl: String, // 홈페이지 정보
    val tel: String, // 관광지 전화번호
    var likeCount: Int, // 좋ㅇ요 수
    val contentId: Int, // 콘텐츠 ID
    val contentTypeId: Int // 콘텐츠 타입 ID
)