package com.arstudios.fliigo.core.network

import com.arstudios.fliigo.balance.data.BalanceResponse
import com.arstudios.fliigo.inventory.data.CategoryDto
import com.arstudios.fliigo.auth.data.LoginRequest
import com.arstudios.fliigo.inventory.data.ProductDto
import com.arstudios.fliigo.auth.data.RegistroInitRequest
import com.arstudios.fliigo.auth.data.RegistroVerifyRequest
import com.arstudios.fliigo.balance.data.SaleRequest
import com.arstudios.fliigo.balance.data.SaleResponse
import com.arstudios.fliigo.SetupStore.data.StoreSetupRequest
import com.arstudios.fliigo.auth.data.UsuarioResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("api/sales/clients/{storeId}")
    suspend fun getUniqueClients(
        @Path("storeId") storeId: Int
    ): Response<List<com.arstudios.fliigo.debt.data.ClientSuggestionDto>>

    @DELETE("api/sales/{id}")
    suspend fun deleteSale(
        @Path("id") saleId: Int
    ): Response<Unit>

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

    // Eliminar un producto por su ID
    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") productId: Int
    ): Response<Unit>

    // Actualizar un producto por su ID
    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Int,
        @Body product: ProductDto
    ): Response<Unit>

    // Coincide con POST /api/store/ de categoryController.js
    @POST("api/store")
    suspend fun createCategory(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    // Coincide con GET /api/store/:storeId de categoryController.js
    @GET("api/store/{storeId}")
    suspend fun getCategoriesByStore(
        @Path("storeId") storeId: Int
    ): Response<List<CategoryDto>>

    // --- DEUDAS ---
    @GET("api/debts/store/{storeId}")
    suspend fun getDebtsByStore(
        @Path("storeId") storeId: Int
    ): Response<List<com.arstudios.fliigo.debt.data.DebtDto>>

    @POST("api/debts/register")
    suspend fun registerDebt(
        @Body debt: com.arstudios.fliigo.debt.data.DebtDto
    ): Response<Unit>

    @DELETE("api/debts/{id}")
    suspend fun deleteDebt(
        @Path("id") debtId: Int
    ): Response<Unit>

    @PUT("api/debts/{id}/pay")
    suspend fun markDebtAsPaid(
        @Path("id") debtId: Int
    ): Response<Unit>

    // --- ESTADÍSTICAS ---
    @GET("api/stats/{storeId}")
    suspend fun getStoreStats(
        @Path("storeId") storeId: Int
    ): Response<com.arstudios.fliigo.stats.data.StatsResponse>
}
