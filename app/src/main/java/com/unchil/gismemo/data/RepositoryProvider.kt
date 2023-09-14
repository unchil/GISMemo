package com.unchil.gismemo.data

import android.util.Log


class RepositoryProvider {


    companion object {

        private var logTag = RepositoryProvider::class.java.name

        private var repository: Repository? = null

        fun getRepository() : Repository  = repository ?: synchronized(this){
            repository ?: createRepository()
        }

        private fun createRepository( ): Repository {
            try {
                val newRepository = Repository()
                repository = newRepository
            }catch (e: Exception) {
                Log.d(logTag, "Fail createRepository:[${e.message}]")
            }
            return repository!!
        }
    }
}
