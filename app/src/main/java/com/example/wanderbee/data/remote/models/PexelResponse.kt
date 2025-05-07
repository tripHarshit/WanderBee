package com.example.wanderbee.data.remote.models

data class PexelsPhotoResponse(
    val photos: List<PexelsPhoto>
)

data class PexelsVideoResponse(
    val videos: List<PexelsVideo>
)


data class PexelsPhoto(
    val id: Int,
    val src: PexelsSrc
)

data class PexelsSrc(
    val medium: String,
    val large: String,
    val original: String
)

data class PexelsVideo(
    val id: Int,
    val video_files: List<PexelsVideoFile>
)

data class PexelsVideoFile(
    val link: String,
    val quality: String,
    val width: Int,
    val height: Int
)
