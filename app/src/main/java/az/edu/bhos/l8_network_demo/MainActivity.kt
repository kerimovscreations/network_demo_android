package az.edu.bhos.l8_network_demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET

class MainActivity : AppCompatActivity() {
    private val vm = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm.usersData.observe(this) { userList ->
            println(userList)
        }

        vm.getUsers()
    }
}

class MainViewModel: ViewModel() {
    private val _usersData: MutableLiveData<List<User>> = MutableLiveData()
    val usersData: LiveData<List<User>> = _usersData

    fun getUsers() {
        val repo = GithubRepository(LocalHttpClient().getGithubService())

        viewModelScope.launch {
            val users = repo.getUsers()

            _usersData.postValue(users)
        }
    }
}

class GithubRepository(private val service: GitHubService) {
    suspend fun getUsers(): List<User> {
        return service.getAllUsers()
    }
}

class LocalHttpClient() {
    private val base_url = "https://62961db375c34f1f3b299286.mockapi.io/"
    private val contentType = MediaType.parse("application/json")!!

    private val client: OkHttpClient

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    private var retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(base_url)
        .addConverterFactory(Json.asConverterFactory(contentType))
        .build()

    fun getGithubService(): GitHubService {
        return retrofit.create<GitHubService>()
    }
}

interface GitHubService {
    @GET("users")
    suspend fun getAllUsers(): List<User>
}

@Serializable
data class User(
    val id: Int,
    val name: String,
    val avatar: String,
    val createdAt: String
)