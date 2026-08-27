package com.samsul.inosoftapps.di

import com.samsul.inosoftapps.data.local.dao.ArticleDao
import com.samsul.inosoftapps.data.local.database.NewsDatabase
import com.samsul.inosoftapps.data.local.database.createNewsDatabase
import com.samsul.inosoftapps.data.remote.KtorClientFactory
import com.samsul.inosoftapps.data.remote.KtorNewsApiService
import com.samsul.inosoftapps.data.remote.NewsApiService
import com.samsul.inosoftapps.data.remote.config.ApiConfigProvider
import com.samsul.inosoftapps.data.remote.config.DefaultApiConfigProvider
import com.samsul.inosoftapps.data.repository.ArticleRepositoryImpl
import com.samsul.inosoftapps.domain.repository.ArticleRepository
import com.samsul.inosoftapps.domain.usecase.GetArticleDetailUseCase
import com.samsul.inosoftapps.domain.usecase.GetArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.RefreshArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.SearchArticlesUseCase
import com.samsul.inosoftapps.presentation.viewmodel.ArticleDetailViewModel
import com.samsul.inosoftapps.presentation.viewmodel.ArticleListViewModel
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Network module providing ApiConfigProvider, HttpClient, and KtorNewsApiService instances.
 */
val networkModule = module {
    single<ApiConfigProvider> { DefaultApiConfigProvider() }
    single<HttpClient> { KtorClientFactory.createHttpClient(configProvider = get()) }
    single<NewsApiService> { KtorNewsApiService(client = get(), configProvider = get()) }
}

/**
 * Database module providing NewsDatabase and ArticleDao.
 */
val databaseModule = module {
    single<NewsDatabase> { createNewsDatabase(get()) }
    single<ArticleDao> { get<NewsDatabase>().articleDao() }
}

/**
 * Repository module binding concrete implementation to domain interface.
 */
val repositoryModule = module {
    single<ArticleRepository> { ArticleRepositoryImpl(get(), get()) }
}

/**
 * UseCase module providing business logic use cases.
 */
val useCaseModule = module {
    factory { GetArticlesUseCase(get()) }
    factory { RefreshArticlesUseCase(get()) }
    factory { GetArticleDetailUseCase(get()) }
    factory { SearchArticlesUseCase(get()) }
}

/**
 * ViewModel module configuring multiplatform ViewModels.
 */
val viewModelModule = module {
    viewModel { ArticleListViewModel(get(), get(), get()) }
    viewModel { ArticleDetailViewModel(get()) }
}

/**
 * Combined list of all application Koin modules.
 */
val appModules: List<Module> = listOf(
    platformModule,
    networkModule,
    databaseModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)

/**
 * Initializes Koin for multiplatform application.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModules)
    }

/**
 * Helper function to initialize Koin on iOS.
 */
fun initKoinIos() = initKoin {}
