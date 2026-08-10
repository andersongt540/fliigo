package com.arstudios.fliigo.data.network

import com.arstudios.fliigo.data.model.BalanceResponse
import com.arstudios.fliigo.data.model.CategoryDto
import com.arstudios.fliigo.data.model.LoginRequest
import com.arstudios.fliigo.data.model.ProductDto
import com.arstudios.fliigo.data.model.RegistroInitRequest
import com.arstudios.fliigo.data.model.RegistroVerifyRequest
import com.arstudios.fliigo.data.model.SaleRequest
import com.arstudios.fliigo.data.model.SaleResponse
import com.arstudios.fliigo.data.model.StoreSetupRequest
import com.arstudios.fliigo.data.model.UsuarioResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/register")
    suspend fun iniciarRegistro(
        @Body request: RegistroInitRequest
    ): Response<UsuarioResponse>

    @POST("api/auth/verify-code")
    suspend fun verificarCodigo(
        @Body request: RegistroVerifyRequest
    ): Response<UsuarioResponse>

    @POST("api/auth/login")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<UsuarioResponse>

    @POST("api/store/setup")
    suspend fun setupStore(
        @Body request: StoreSetupRequest
    ): Response<UsuarioResponse>

    @GET("api/store/balance/{userId}")
    suspend fun getStoreBalance(
        @Path("userId") userId: Int
    ): Response<BalanceResponse>

    @POST("api/sales/register")
    suspend fun registerSale(
        @Body request: SaleRequest
    ): Response<SaleResponse>

    // Obtener los productos de la tienda por su ID
    @GET("api/products/store/{storeId}")
    suspend fun getProductsByStore(
        @Path("storeId") storeId: Int
    ): Response<List<ProductDto>>

    // Registrar un nuevo producto en la tienda
    @POST("api/products/register")
    suspend fun registerProduct(
        @Body product: ProductDto
    ): Response<Unit>

    // Crear una nueva categoría para la tienda
    @POST("api/store/category")
    suspend fun createCategory(
        @Body request: Map<String, Any>
    ): Response<Unit>

    @GET("api/store/categories/{storeId}")
    suspend fun getCategoriesByStore(
        @Path("storeId") storeId: Int
    ): Response<List<CategoryDto>>
}