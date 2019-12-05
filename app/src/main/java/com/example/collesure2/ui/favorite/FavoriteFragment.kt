package com.example.collesure2.ui.favorite

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.collesure2.R
import com.example.collesure2.data.ImageItem
import com.example.collesure2.data.repository.AppDB
import com.example.collesure2.data.repository.favorite.Favorite
import com.example.collesure2.ui.list.RecyclerFragment
import kotlinx.coroutines.*

class FavoriteFragment : Fragment(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch(Dispatchers.Main) {
            async(Dispatchers.Default) {
                searchFavorite("")
            }.await().let {
                showRecycler(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.item_favorite_actionbar, menu)
        val searchView = menu.findItem(R.id.favorite_search).actionView as SearchView

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(text: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                GlobalScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.Default) {
                        searchFavorite(text!!?:"")
                    }.let {
                        showRecycler(it)
                    }
                }
                return false
            }
        })
    }

    private fun showRecycler(imageList: ArrayList<ImageItem>) {
        val recyclerFragment = RecyclerFragment()
        val fragmentManager = fragmentManager
        val bundle = Bundle()
        bundle.putSerializable("imageList", imageList)
        recyclerFragment.arguments = bundle
        fragmentManager!!.beginTransaction().replace(R.id.favorite_view, recyclerFragment).commit()

    }
    private fun searchFavorite(word: String): ArrayList<ImageItem> {
        var query = "%" + word.split(" ").joinToString(separator = "%") + "%"

        val db = AppDB.getInstance(context!!)
        var list: List<Favorite> = listOf<Favorite>()
        if(query == "") {
            list = db.favoriteDao().getAll()
        } else {
            list = db.favoriteDao().searchByTag(query)
        }

        val imageList = arrayListOf<ImageItem>()

        for (fav in list) {
            imageList.add(ImageItem.fromFavorite(fav))
        }
        return imageList
    }
}