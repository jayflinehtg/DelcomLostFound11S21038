package com.ifs21038.lostfounds.presentation.main

import com.ifs21038.lostfounds.R
import com.ifs21038.lostfounds.adapter.LostFoundsAdapter
import com.ifs21038.lostfounds.data.remote.MyResult
import com.ifs21038.lostfounds.databinding.ActivityMainBinding
import com.ifs21038.lostfounds.presentation.ViewModelFactory
import com.ifs21038.lostfounds.presentation.login.LoginActivity
import com.ifs21038.lostfounds.presentation.lostfound.LostFoundDetailActivity
import com.ifs21038.lostfounds.presentation.lostfound.LostFoundFavoriteActivity
import com.ifs21038.lostfounds.presentation.lostfound.LostFoundManageActivity
import com.ifs21038.lostfounds.presentation.profile.ProfileActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs18005.delcomtodo.data.remote.response.DelcomLostFoundsResponse
import com.ifs18005.delcomtodo.data.remote.response.LostFoundsItemResponse
import com.ifs21038.lostfounds.helper.Utils.Companion.observeOnce

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == LostFoundManageActivity.RESULT_CODE) {
            recreate()
        }

        if (result.resultCode == LostFoundDetailActivity.RESULT_CODE) {
            result.data?.let {
                val isChanged = it.getBooleanExtra(
                    LostFoundDetailActivity.KEY_IS_CHANGED,
                    false
                )

                if (isChanged) {
                    recreate()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
    }

    private fun setupView() {
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)

        binding.appbarMain.overflowIcon =
            ContextCompat
                .getDrawable(this, R.drawable.ic_more_vert_24)

        observeGetLostFounds(null, null, null)
    }

    private fun setupAction() {
        binding.appbarMain.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.mainMenuProfile -> {
                    openProfileActivity()
                    true
                }

                R.id.mainMenuLogout -> {
                    viewModel.logout()
                    openLoginActivity()
                    true
                }

                R.id.mainMenuFavoriteTodos -> {
                    openFavoriteLostFoundActivity()
                    true
                }

                R.id.filter ->{
                    val checkedItems = booleanArrayOf(false, false, false, false, false)
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder
                        .setTitle("Pilih item-item yang ingin ditampilkan")
                        .setPositiveButton("Pilih") { dialog, which ->
                            val saya = if (checkedItems[0]) 1 else null

                            val lostorfound: String? = if(checkedItems[1]) {
                                if(checkedItems[2]) {
                                    null
                                } else {
                                    "Lost"
                                }
                            } else {
                                if(checkedItems[2]) {
                                    "Found"
                                } else {
                                    null
                                }
                            }

                            val status: Int? = if(checkedItems[3]) {
                                if(checkedItems[4]) {
                                    null
                                } else {
                                    1
                                }
                            } else {
                                if(checkedItems[4]) {
                                    0
                                } else {
                                    null
                                }
                            }

                            observeGetLostFounds(status, saya, lostorfound)
                        }
                        .setNegativeButton("Batal") { dialog, which ->
                            // Do something else.
                        }
                        .setMultiChoiceItems(
                            arrayOf("Item Saya", "Lost", "Found", "Completed", "Incompleted"), checkedItems) { dialog, which, isChecked ->
                            checkedItems[which] = isChecked
                        }

//                        Log.d("CheckedItemsDump", "Checked items: ${checkedItems.contentToString()}")

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                    true
                }
                else -> false
            }
        }


        binding.fabMainAddLostFound.setOnClickListener {
            openAddLostFoundActivity()
        }

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                openLoginActivity()
            } else {
                // Load Lost-Founds
            }
        }
    }

    private fun observeGetLostFounds(
        isCompleted: Int?,
        isMe: Int?,
        status: String?
    ) {
        viewModel.getLostFounds(isCompleted,isMe,status).observe(this) { result ->
            if (result != null) {
                when (result) {
                    is MyResult.Loading -> {
                        showLoading(true)
                    }

                    is MyResult.Success -> {
                        showLoading(false)
                        loadAllToLayout(result.data)
                    }

                    is MyResult.Error -> {
                        showLoading(false)
                        showEmptyError(true)
                    }
                }
            }
        }
    }



    private fun observeGetMyLostFounds() {
        // Panggil fungsi getLostandFounds() dengan menyertakan nilai isMe
        viewModel.getLostFound().observe(this) { result ->
            if (result != null) {
                when (result) {
                    is MyResult.Loading -> {
                        showLoading(true)
                    }
                    is MyResult.Success -> {
                        showLoading(false)
                        loadLostFoundsToLayout(result.data)
                    }
                    is MyResult.Error -> {
                        showLoading(false)
                        showEmptyError(true)
                    }
                }
            }
        }
    }

    private fun loadLostFoundsToLayout(response: DelcomLostFoundsResponse) {
        if (response == null) {
            // Handle null case appropriately, misalnya menampilkan pesan error atau melakukan tindakan lainnya
            Log.e("MainActivity", "response == null")
            return
        } else if (response.data == null){
            Log.e("MainActivity", "response.data == null")
            return
        } else if (response.data.lostFounds == null){
            Log.e("MainActivity", "response.data.lostfounds == null")
            return
        }

        val lostfounds = response.data.lostFounds
        val layoutManager = LinearLayoutManager(this)
        binding.rvMainLostFounds.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvMainLostFounds.addItemDecoration(itemDecoration)

        if (lostfounds.isEmpty()) {
            showEmptyError(true)
            binding.rvMainLostFounds.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)

            val adapter = LostFoundsAdapter()
            adapter.submitOriginalList(lostfounds)
            binding.rvMainLostFounds.adapter = adapter
            adapter.setOnItemClickCallback(object : LostFoundsAdapter.OnItemClickCallback {
                override fun onCheckedChangeListener(
                    lostfound: LostFoundsItemResponse,
                    isCompleted: Boolean
                ) {
                    adapter.filter(binding.svMain.query.toString())

                    viewModel.putLostFound(
                        lostfound.id,
                        lostfound.title,
                        lostfound.description,
                        lostfound.status,
                        isCompleted
                    ).observeOnce {
                        when (it) {
                            is MyResult.Error -> {
                                if (isCompleted) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Gagal menyelesaikan lostfound: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Gagal batal menyelesaikan lostfound: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            is MyResult.Success -> {
                                if (isCompleted) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Berhasil menyelesaikan lostfound: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Berhasil batal menyelesaikan lostfound: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            else -> {}
                        }
                    }
                }

                override fun onClickDetailListener(lostfoundId: Int) {
                    val intent = Intent(
                        this@MainActivity,
                        LostFoundDetailActivity::class.java
                    )
                    intent.putExtra(LostFoundDetailActivity.KEY_LOST_FOUND_ID, lostfoundId)
                    launcher.launch(intent)
                }
            })

            binding.svMain.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvMainLostFounds.layoutManager?.scrollToPosition(0)
                        return true
                    }
                })
        }
    }

    private fun loadAllToLayout(response: DelcomLostFoundsResponse) {
        // Periksa apakah response atau data pada response null
        if (response == null) {
            // Handle null case appropriately, misalnya menampilkan pesan error atau melakukan tindakan lainnya
            Log.e("MainActivity", "response == null")
            return
        } else if (response.data == null){
            Log.e("MainActivity", "response.data == null")
            return
        } else if (response.data.lostFounds == null){
            Log.e("MainActivity", "response.data.todos == null")
            return
        }

        // Lanjutkan dengan pemrosesan data
        val todos = response.data.lostFounds
        val layoutManager = LinearLayoutManager(this)
        binding.rvMainLostFounds.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvMainLostFounds.addItemDecoration(itemDecoration)

        if (todos.isEmpty()) {
            showEmptyError(true)
            binding.rvMainLostFounds.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)

            val adapter = LostFoundsAdapter()
            adapter.submitOriginalList(todos)
            binding.rvMainLostFounds.adapter = adapter
            adapter.setOnItemClickCallback(object : LostFoundsAdapter.OnItemClickCallback {
                override fun onCheckedChangeListener(
                    todo: LostFoundsItemResponse,
                    isChecked: Boolean
                ) {
                    adapter.filter(binding.svMain.query.toString())

                    viewModel.putLostFound(
                        todo.id,
                        todo.title,
                        todo.description,
                        todo.status,
                        isChecked
                    ).observeOnce {
                        when (it) {
                            is MyResult.Error -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Gagal menyelesaikan Lost And Found: " + todo.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Gagal menyelesaikan Lost And Found: " + todo.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            is MyResult.Success<*> -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Berhasil menyelesaikan Lost And Found: " + todo.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Berhasil batal menyelesaikan Lost And Found: " + todo.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            else -> {}
                        }
                    }
                }

                override fun onClickDetailListener(todoId: Int) {
                    val intent = Intent(
                        this@MainActivity,
                        LostFoundDetailActivity::class.java
                    )
                    intent.putExtra(LostFoundDetailActivity.KEY_LOST_FOUND_ID, todoId)
                    launcher.launch(intent)
                }
            })

            binding.svMain.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvMainLostFounds.layoutManager?.scrollToPosition(0)
                        return true
                    }
                }
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbMain.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    private fun openProfileActivity() {
        val intent = Intent(applicationContext, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun showComponentNotEmpty(status: Boolean) {
        binding.svMain.visibility =
            if (status) View.VISIBLE else View.GONE

        binding.rvMainLostFounds.visibility =
            if (status) View.VISIBLE else View.GONE
    }

    private fun showEmptyError(isError: Boolean) {
        binding.tvMainEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }

    private fun openLoginActivity() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun openAddLostFoundActivity() {
        val intent = Intent(
            this@MainActivity,
            LostFoundManageActivity::class.java
        )
        intent.putExtra(LostFoundManageActivity.KEY_IS_ADD, true)
        launcher.launch(intent)
    }

    private fun openFavoriteLostFoundActivity() {
        val intent = Intent(
            this@MainActivity,
            LostFoundFavoriteActivity::class.java
        )
        launcher.launch(intent)
    }
}