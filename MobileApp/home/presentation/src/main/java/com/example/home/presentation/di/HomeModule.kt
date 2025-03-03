package com.example.home.presentation.di

import com.example.home.presentation.DashboardVM
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    viewModelOf(::DashboardVM)
}