package com.example.products.presentation.di

import com.example.products.domain.validators.ProductDataValidator
import com.example.products.presentation.viewModels.ProductAddVM
import com.example.products.presentation.viewModels.ProductDetailVM
import com.example.products.presentation.viewModels.ProductsListVM
import com.example.products.presentation.viewModels.ProductsVM
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val productsModule = module {
    viewModelOf(::ProductsVM)
    viewModelOf(::ProductsListVM)
    viewModelOf(::ProductAddVM)
    viewModelOf(::ProductDetailVM)
    singleOf(::ProductDataValidator)
}