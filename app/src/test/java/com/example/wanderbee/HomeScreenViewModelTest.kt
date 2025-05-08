package com.example.wanderbee

import com.example.wanderbee.data.remote.models.PexelsPhoto
import com.example.wanderbee.data.remote.models.PexelsPhotoResponse
import com.example.wanderbee.data.remote.models.PexelsSrc
import com.example.wanderbee.data.repository.DefaultPexelsRepository
import com.example.wanderbee.screens.home.HomeScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.Dispatcher
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest{

    lateinit var defaultPexelsRepository: DefaultPexelsRepository
    lateinit var viewModel: HomeScreenViewModel

    val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        defaultPexelsRepository = mockk()
        viewModel = HomeScreenViewModel(
            defaultPexelsRepository
        )
    }

    @After
     fun tearDown(){
         Dispatchers.resetMain()
     }

    @Test
    fun `loadCityCoverImage fetches and stores image URL`() = runTest{
        val cityName = "Paris"
        val mockPhotoList = List(5){
            PexelsPhoto(
                id = it,
                src = PexelsSrc(
                    medium = "http://example.com/image$it.jpg)"
                ))
        }
        val mockResponse = PexelsPhotoResponse(photos = mockPhotoList)

        coEvery {
            defaultPexelsRepository.getPexelsPhotos(cityName)
        }returns mockResponse

        viewModel.loadCityCoverImage(cityName)
        advanceUntilIdle()

        val imageUrl = viewModel.imageUrls[cityName]
        assertNotNull(imageUrl)
        assertTrue(imageUrl!!.startsWith("http://example.com/image"))
    }

    @Test
    fun  `loadCityCoverImage sets null when exception occurs`() = runTest{

        val cityName = "InvalidCity"
        coEvery { defaultPexelsRepository.getPexelsPhotos(cityName) } throws RuntimeException ("trying to fetch invalid image")

        viewModel.loadCityCoverImage(cityName)
        advanceUntilIdle()

        assertNull(viewModel.imageUrls[cityName])
    }




}