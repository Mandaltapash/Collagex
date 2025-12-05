package com.tods.project_olx.activity

import android.view.LayoutInflater
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityMainBinding
import com.tods.project_olx.databinding.CategoryItemBinding
import com.tods.project_olx.helper.RecyclerItemClickListener
import com.tods.project_olx.helper.ThemeManager
import com.tods.project_olx.adapter.AdapterAd
import com.tods.project_olx.model.Ad
import com.tods.project_olx.activity.ChatListActivity

data class Category(val name: String, val iconResId: Int)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var auth: FirebaseAuth = Firebase.auth

    private lateinit var adsRef: DatabaseReference
    private val allAds: MutableList<Ad> = mutableListOf()
    private val filteredAds: MutableList<Ad> = mutableListOf()
    private lateinit var adsAdapter: AdapterAd
    private var selectedCategory: String? = null

    // Advanced filters
    private var filterStatus: String? = null
    private var filterMinPrice: Long? = null
    private var filterMaxPrice: Long? = null
    private var filterDate: String? = null
    private var filterSeller: String? = null // New variable
    private var filterMinRating: Int? = null // New variable
    private var filterHasReviews: Boolean? = null // New variable
    private val categoriesLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val category = data?.getStringExtra("selectedCategory")
            selectedCategory = category
            applyFilters()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before creating the activity
        ThemeManager.applyTheme(ThemeManager.getTheme(this))
        
        super.onCreate(savedInstanceState)
        configViewBinding()
        // setSupportActionBar(binding.toolbar)

        configCategoriesRecyclerView()
        configAdsRecyclerView()
        // configSearchBar()
        configBottomNav()
        // configFabSellCenter()
        // configTopBarButtons()
        configClickListeners()

        binding.searchBar.addTextChangedListener {
            applyFilters()
        }

        loadAds()

        if (intent.hasExtra("selectedCategory")) {
            selectedCategory = intent.getStringExtra("selectedCategory")
            applyFilters()
        }
    }

    private fun configCategoriesRecyclerView() {
        val categories = listOf(
            Category("Furniture", R.drawable.furniture),
            Category("Electronics", R.drawable.electronics),
            Category("Fashion", R.drawable.fashion),
            Category("Books", R.drawable.books),
            Category("Others", R.drawable.others),
            Category("Pets", R.drawable.pets),
            Category("Freezone", R.drawable.freezone),
            Category("Sports", R.drawable.sports)
        )

        val categoryAdapter = CategoryAdapter(categories) { category ->
            selectedCategory = if (selectedCategory == category.name) null else category.name
            applyFilters()
        }

        binding.recyclerCategories.layoutManager = GridLayoutManager(this, 4)
        binding.recyclerCategories.adapter = categoryAdapter
    }

    private fun configClickListeners() {
        /*
        binding.locationBar.setOnClickListener {
            startActivity(Intent(applicationContext, LocationActivity::class.java))
        }
        */
        // Main filter chip
        binding.buttonFilter.setOnClickListener {
            showFilterDialog()
        }
        
        // Individual filter chips
        binding.chipPrice.setOnClickListener {
            showFilterDialog() // Opens filter dialog focused on price
        }
        
        binding.chipCategory.setOnClickListener {
            val intent = Intent(applicationContext, CategoriesActivity::class.java)
            categoriesLauncher.launch(intent)
        }
        
        binding.chipLocation.setOnClickListener {
            showFilterDialog() // Opens filter dialog focused on location
        }
        
        binding.textSeeMoreCategories.setOnClickListener {
            val intent = Intent(applicationContext, CategoriesActivity::class.java)
            categoriesLauncher.launch(intent)
        }
        binding.textSeeMoreFeatured.setOnClickListener {
            Toast.makeText(this, "See more featured clicked", Toast.LENGTH_SHORT).show()
        }
    }

/*
    private fun configTopBarButtons() {
        binding.buttonTopSearch.setOnClickListener {
            Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTopBell.setOnClickListener {
            startActivity(Intent(applicationContext, NotificationsActivity::class.java))
        }
    }
*/

/*
    private fun configFabSellCenter() {
        binding.fabSellCenter.setOnClickListener {
            startActivity(Intent(applicationContext, RegisterAddActivity::class.java))
        }
    }
*/

    private fun configBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    true
                }
                R.id.nav_chats -> {
                    if (auth.currentUser == null) {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(applicationContext, ChatListActivity::class.java))
                    }
                    true
                }
                R.id.nav_sell -> {
                    if (auth.currentUser == null) {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(applicationContext, RegisterAddActivity::class.java))
                    }
                    true
                }
                R.id.nav_my_ads -> {
                    startActivity(Intent(applicationContext, MyAdsActivity::class.java))
                    true
                }
                R.id.nav_account -> {
                    if (auth.currentUser == null) {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun configViewBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun configAdsRecyclerView() {
        adsAdapter = AdapterAd(filteredAds)
        val gridLayoutManager = GridLayoutManager(this, 2)
        binding.recyclerAds.layoutManager = gridLayoutManager
        binding.recyclerAds.setHasFixedSize(true)
        binding.recyclerAds.adapter = adsAdapter

        binding.recyclerAds.addOnItemTouchListener(
            RecyclerItemClickListener(this, binding.recyclerAds, object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: android.view.View, position: Int) {
                    val selectedAd = filteredAds[position]
                    val intent = Intent(applicationContext, AdDetailsActivity::class.java)
                    intent.putExtra("selectedAd", selectedAd)
                    startActivity(intent)
                }

                override fun onItemLongClick(view: android.view.View?, position: Int) {
                    // no-op for now
                }
            })
        )
    }

/*
    private fun configSearchBar() {
        binding.searchBar.addTextChangedListener { text ->
            applyFilters(text?.toString() ?: "")
        }

*/

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filters, null)
        val spinnerStatus = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerStatus)
        val spinnerDate = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerDate)
        val editMinPrice = dialogView.findViewById<android.widget.EditText>(R.id.editMinPrice)
        val editMaxPrice = dialogView.findViewById<android.widget.EditText>(R.id.editMaxPrice)
        val editSeller = dialogView.findViewById<android.widget.EditText>(R.id.editSeller) // Get reference
        val seekBarRating = dialogView.findViewById<android.widget.SeekBar>(R.id.seekBarRating)
        val textRatingValue = dialogView.findViewById<android.widget.TextView>(R.id.textRatingValue)
        val switchHasReviews = dialogView.findViewById<android.widget.Switch>(R.id.switchHasReviews)

        // Status spinner
        val statuses = listOf("Any", "available", "sold")
        val statusAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter
        filterStatus?.let { s ->
            val index = statuses.indexOf(s)
            if (index >= 0) spinnerStatus.setSelection(index)
        }

        // Date spinner
        val dates = listOf("Newest first", "Oldest first")
        val dateAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, dates)
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDate.adapter = dateAdapter
        filterDate?.let { s ->
            val index = dates.indexOf(s)
            if (index >= 0) spinnerDate.setSelection(index)
        }

        // Prefill prices
        filterMinPrice?.let { editMinPrice.setText(it.toString()) }
        filterMaxPrice?.let { editMaxPrice.setText(it.toString()) }

        // Prefill seller
        filterSeller?.let { editSeller.setText(it) }

        // Prefill rating
        filterMinRating?.let {
            seekBarRating.progress = it
            textRatingValue.text = "${it} stars"
        } ?: run {
            seekBarRating.progress = 0
            textRatingValue.text = "0 stars"
        }

        seekBarRating.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                textRatingValue.text = "${progress} stars"
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // Prefill has reviews switch
        filterHasReviews?.let { switchHasReviews.isChecked = it }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<android.widget.Button>(R.id.buttonClearFilters).setOnClickListener {
            filterStatus = null
            filterMinPrice = null
            filterMaxPrice = null
            filterDate = null
            filterSeller = null // Clear seller filter
            filterMinRating = null // Clear rating filter
            filterHasReviews = null // Clear has reviews filter
            applyFilters()
            dialog.dismiss()
        }

        dialogView.findViewById<android.widget.Button>(R.id.buttonApplyFilters).setOnClickListener {
            val statusSelected = spinnerStatus.selectedItem as String
            filterStatus = if (statusSelected == "Any") null else statusSelected

            val dateSelected = spinnerDate.selectedItem as String
            filterDate = dateSelected

            val minPriceText = editMinPrice.text.toString().trim()
            val maxPriceText = editMaxPrice.text.toString().trim()
            filterMinPrice = minPriceText.toLongOrNull()
            filterMaxPrice = maxPriceText.toLongOrNull()

            filterSeller = editSeller.text.toString().trim() // Update seller filter
            filterMinRating = seekBarRating.progress // Update rating filter
            filterHasReviews = switchHasReviews.isChecked // Update has reviews filter

            applyFilters()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadAds() {
        adsRef = FirebaseDatabase.getInstance().getReference("ads_all")
        adsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allAds.clear()
                for (ds in snapshot.children) {
                    val ad = ds.getValue(Ad::class.java)
                    if (ad != null) {
                        allAds.add(ad)
                    }
                }
                applyFilters()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error loading ads", Toast.LENGTH_SHORT).show()
            }
        })
    }

        private fun applyFilters() {

            android.util.Log.d("MainActivity", "applyFilters: selectedCategory = $selectedCategory")

            val query = binding.searchBar.text.toString()

            val lowercaseQuery = query.lowercase()

            var ads = allAds.toList()

    

            if (filterDate == "Newest first") {

                ads = ads.sortedByDescending { it.createdAt }

            } else if (filterDate == "Oldest first") {

                ads = ads.sortedBy { it.createdAt }

            }

    

            filteredAds.clear()

    


            for (ad in ads) {

                val matchesText = if (lowercaseQuery.isEmpty()) {

                    true

                } else {

                    ad.title.lowercase().contains(lowercaseQuery) || ad.description.lowercase().contains(lowercaseQuery)

                }

    

                val matchesCategory = selectedCategory?.let { cat ->

                    ad.category.equals(cat, ignoreCase = true)

                } ?: true

    

                            val matchesStatus = filterStatus?.let { st ->

    

                                ad.status.equals(st, ignoreCase = true)

    

                            } ?: true

    

                val matchesPrice = run {

                    val p = ad.price

                    val minOk = filterMinPrice?.let { p >= it } ?: true

                    val maxOk = filterMaxPrice?.let { p <= it } ?: true

                    minOk && maxOk

                }

    

                val matchesSeller = filterSeller?.let { seller ->

                    ad.sellerName.lowercase().contains(seller.lowercase())

                } ?: true

    

                val matchesRating = filterMinRating?.let { minRating ->

                    ad.ratingAverage >= minRating

                } ?: true

    

                val matchesReviews = filterHasReviews?.let { hasReviews ->

                    if (hasReviews) ad.ratingCount > 0 else true

                } ?: true

    
                // Filter out ads posted by the current user
                val isNotOwnAd = run {
                    val currentUserId = auth.currentUser?.uid
                    if (currentUserId != null) {
                        ad.sellerId != currentUserId
                    } else {
                        true // If not logged in, show all ads
                    }
                }

    

                if (matchesText && matchesCategory && matchesStatus && matchesPrice && matchesSeller && matchesRating && matchesReviews && isNotOwnAd) {

                    filteredAds.add(ad)

                }

            }


            android.util.Log.d("MainActivity", "applyFilters: filteredAds.size = ${filteredAds.size}")

            adsAdapter.notifyDataSetChanged()

        }

/*
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
*/

/*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_log_in -> {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                true
            }
            R.id.menu_log_off -> {
                auth.signOut()
                invalidateOptionsMenu()
                true
            }
            R.id.menu_register -> {
                startActivity(Intent(applicationContext, RegisterActivity::class.java))
                true
            }
            R.id.menu_my_adds -> {
                startActivity(Intent(applicationContext, MyAdsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
*/

/*
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (auth.currentUser == null){
            menu!!.setGroupVisible(R.id.group_logged_out, true)
            menu.setGroupVisible(R.id.group_logged_in, false)
        } else {
            menu!!.setGroupVisible(R.id.group_logged_in, true)
            menu.setGroupVisible(R.id.group_logged_out, false)
        }
        return super.onPrepareOptionsMenu(menu)
    }
*/
}

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategorySelected: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.categoryName.text = category.name
        holder.binding.categoryIcon.setImageResource(category.iconResId)
        holder.binding.root.setOnClickListener {
            onCategorySelected(category)
        }
    }

    override fun getItemCount() = categories.size
}

