package com.ifs21038.lostfounds.presentation.lostfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs18005.delcomtodo.data.remote.response.LostFoundsItemResponse

import com.ifs21038.lostfounds.R
import com.ifs21038.lostfounds.adapter.LostFoundsAdapter
import com.ifs21038.lostfounds.data.local.entity.DelcomLostFoundEntity
import com.ifs21038.lostfounds.data.remote.MyResult
import com.ifs21038.lostfounds.databinding.ActivityLostfoundFavoriteBinding
import com.ifs21038.lostfounds.helper.Utils.Companion.entitiesToResponses
import com.ifs21038.lostfounds.helper.Utils.Companion.observeOnce
import com.ifs21038.lostfounds.presentation.ViewModelFactory

class LostFoundFavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostfoundFavoriteBinding
    private val viewModel by viewModels<LostFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
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
        binding = ActivityLostfoundFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }
    private fun setupAction() {
        binding.appbarTodoFavorite.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(LostFoundDetailActivity.KEY_IS_CHANGED, true)
            setResult(LostFoundDetailActivity.RESULT_CODE, resultIntent)
            finishAfterTransition()
        }
    }
    private fun setupView() {
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)
        binding.appbarTodoFavorite.overflowIcon =
            ContextCompat
                .getDrawable(this, R.drawable.ic_more_vert_24)
        observeGetTodos()
    }
    private fun observeGetTodos() {
        viewModel.getLocalTodos().observe(this) { todos ->
            loadTodosToLayout(todos)
        }
    }
    private fun loadTodosToLayout(todos: List<DelcomLostFoundEntity>?) {
        showLoading(false)
        val layoutManager = LinearLayoutManager(this)
        binding.rvTodoFavoriteTodos.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvTodoFavoriteTodos.addItemDecoration(itemDecoration)
        if (todos.isNullOrEmpty()) {
            showEmptyError(true)
            binding.rvTodoFavoriteTodos.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)
            val adapter = LostFoundsAdapter()
            adapter.submitOriginalList(entitiesToResponses(todos))
            binding.rvTodoFavoriteTodos.adapter = adapter
            adapter.setOnItemClickCallback(
                object : LostFoundsAdapter.OnItemClickCallback {
                    override fun onCheckedChangeListener(
                        todo: LostFoundsItemResponse,
                        isChecked: Boolean
                    ) {
                        adapter.filter(binding.svTodoFavorite.query.toString())
                        val newTodo = DelcomLostFoundEntity(
                            id = todo.id,
                            title = todo.title,
                            description = todo.description,
                            isFinished = todo.isCompleted,
                            cover = todo.cover,
                            createdAt = todo.createdAt,
                            updatedAt = todo.updatedAt,
                        )
                        viewModel.putLostFound(
                            todo.id,
                            todo.title,
                            todo.description,
                            isChecked
                        ).observeOnce {
                            when (it) {
                                is MyResult.Error -> {
                                    if (isChecked) {
                                        Toast.makeText(
                                            this@LostFoundFavoriteActivity,
                                            "Gagal menyelesaikan todo: " + todo.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@LostFoundFavoriteActivity,
                                            "Gagal batal menyelesaikan todo: " + todo.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                is MyResult.Success -> {
                                    if (isChecked) {
                                        Toast.makeText(
                                            this@LostFoundFavoriteActivity,
                                            "Berhasil menyelesaikan todo: " + todo.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@LostFoundFavoriteActivity,
                                            "Berhasil batal menyelesaikan todo: " + todo.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    viewModel.insertLocalTodo(newTodo)
                                }
                                else -> {}
                            }
                        }
                    }
                    override fun onClickDetailListener(todoId: Int) {
                        val intent = Intent(
                            this@LostFoundFavoriteActivity,
                            LostFoundDetailActivity::class.java
                        )
                        intent.putExtra(LostFoundDetailActivity.KEY_TODO_ID, todoId)
                        launcher.launch(intent)
                    }
                })
            binding.svTodoFavorite.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }
                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvTodoFavoriteTodos
                            .layoutManager?.scrollToPosition(0)

                        return true
                    }
                })
        }
    }

    private fun showComponentNotEmpty(status: Boolean) {
        binding.svTodoFavorite.visibility =
            if (status) View.VISIBLE else View.GONE
        binding.rvTodoFavoriteTodos.visibility =
            if (status) View.VISIBLE else View.GONE
    }
    private fun showEmptyError(isError: Boolean) {
        binding.tvTodoFavoriteEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }
    private fun showLoading(isLoading: Boolean) {
        binding.pbTodoFavorite.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
}