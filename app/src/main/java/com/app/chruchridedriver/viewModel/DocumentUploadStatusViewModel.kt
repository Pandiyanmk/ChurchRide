package com.app.chruchridedriver.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.chruchridedriver.data.model.UploadedDocStatus
import com.app.chruchridedriver.data.model.UploadedDocument
import com.app.chruchridedriver.data.model.UploadedDocumentImage
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.NetworkState
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class DocumentUploadStatusViewModel constructor(private val authCheckRepository: MainRepository) :
    ViewModel() {


    val loading = MutableLiveData<Boolean>()

    private val _responseContent = MutableLiveData<UploadedDocument>()
    val responseContent: LiveData<UploadedDocument>
        get() = _responseContent


    private val _registerContent = MutableLiveData<UploadedDocumentImage>()
    val registerContent: LiveData<UploadedDocumentImage>
        get() = _registerContent

    private val _docStatus = MutableLiveData<UploadedDocStatus>()
    val docStatus: LiveData<UploadedDocStatus>
        get() = _docStatus


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    /* Get Uploaded Document Content From Api */
    fun getUploadedDocument(driverId: String) {
        viewModelScope.launch {
            authCheckRepository.getUploadedDocumentResponse(driverId).flowOn(Dispatchers.IO)
                .catch { }.collect { response ->
                    stopLoader()
                    when (response) {
                        is NetworkState.Success -> {
                            _responseContent.value = response.data!!
                        }

                        is NetworkState.Error -> {
                            _errorMessage.value = response.errorMessage
                        }
                    }
                }
        }
    }

    //Update Driver Document  Details
    fun updateDocument(documentId: String, imageUrl: String) {
        viewModelScope.launch {
            authCheckRepository.updateDocument(documentId, imageUrl).flowOn(Dispatchers.IO)
                .catch { }.collect { response ->
                    stopLoader()
                    when (response) {
                        is NetworkState.Success -> {
                            _registerContent.value = response.data!!
                        }

                        is NetworkState.Error -> {
                            _errorMessage.value = response.errorMessage
                        }
                    }
                }
        }
    }

    fun uploadImageToFirebase(storageReference: StorageReference, imageUri: Uri) {
        viewModelScope.launch {
            authCheckRepository.uploadImageToFirebase(storageReference, imageUri)
        }
    }

    //Update Driver Document  Details
    fun updateDocStatus(documentId: String, docStatus: String, comment: String) {
        viewModelScope.launch {
            authCheckRepository.updateDocStatus(documentId, docStatus, comment)
                .flowOn(Dispatchers.IO).catch { }.collect { response ->
                    stopLoader()
                    when (response) {
                        is NetworkState.Success -> {
                            _docStatus.value = response.data!!
                        }

                        is NetworkState.Error -> {
                            _errorMessage.value = response.errorMessage
                        }
                    }
                }
        }
    }

    private fun stopLoader() {
        loading.value = false
    }
}