package com.tods.project_olx.activity

import android.view.LayoutInflater
import android.content.Intent
import android.content.Context
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
import com.tods.project_olx.helper.LocaleManager
import com.tods.project_olx.adapter.AdapterAd
import com.tods.project_olx.model.Ad
import com.tods.project_olx.activity.ChatListActivity

data class Category(val name: String, val iconResId: Int)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adsAdapter: AdapterAd
    private lateinit var database: FirebaseDatabase
    private lateinit var adsRef: DatabaseReference
    private val allAds = ArrayList<Ad>()
    private val filteredAds = ArrayList<Ad>()
    private val auth = FirebaseAuth.getInstance()

    private var selectedCategory: String? = null

    // Advanced filters
    private var filterStatus: String? = null
    private var filterDate: String = "Newest"
    private var filterMinPrice: Long? = null
    private var filterMaxPrice: Long? = null
    private var filterDistrict: String? = null
    private var filterCollege: String? = null
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

    override fun attachBaseContext(newBase: Context) {
        val language = LocaleManager.getLocale(newBase)
        super.attachBaseContext(LocaleManager.setLocale(newBase, language))
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
        val spinnerDistrict = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerDistrict)
        val spinnerCollege = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinnerCollege)

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

        // Setup district dropdown - All 33 districts of Assam
        val districts = listOf(
            "Any",
            "Baksa", "Barpeta", "Biswanath", "Bongaigaon", "Cachar",
            "Charaideo", "Chirang", "Darrang", "Dhemaji", "Dhubri",
            "Dibrugarh", "Dima Hasao", "Goalpara", "Golaghat", "Hailakandi",
            "Hojai", "Jorhat", "Kamrup", "Kamrup Metropolitan", "Karbi Anglong",
            "Karimganj", "Kokrajhar", "Lakhimpur", "Majuli", "Morigaon",
            "Nagaon", "Nalbari", "Sivasagar", "Sonitpur", "South Salmara-Mankachar",
            "Tinsukia", "Udalguri", "West Karbi Anglong"
        )
        
        // District to Colleges mapping
        val districtToColleges = mapOf(
            "Any" to listOf("Any", "Cotton University", "Gauhati University", "IIT Guwahati", "Dibrugarh University", "Tezpur University", "NIT Silchar", "Other Colleges"),
            "Kamrup Metropolitan" to listOf("Any", "Cotton University", "Gauhati University", "IIT Guwahati", "Assam Engineering College", "Royal Global University", "Assam Don Bosco University", "B Borooah College", "Handique Girls College", "Pandu College", "Arya Vidyapeeth College"),
            "Kamrup" to listOf("Any", "Kumar Bhaskar Varma Sanskrit College", "Nalbari College"),
            "Dibrugarh" to listOf("Any", "Dibrugarh University", "Assam Medical College Dibrugarh", "Dibrugarh Hanumanbux Surajmal Kanoi College"),
            "Jorhat" to listOf("Any", "Jorhat Engineering College", "Jorhat Institute of Science & Technology", "Jorhat Medical College", "JB College"),
            "Sonitpur" to listOf("Any", "Tezpur University", "Girijananda Chowdhury Institute of Management & Technology", "Tezpur Medical College"),
            "Cachar" to listOf("Any", "Assam University Silchar", "NIT Silchar", "Silchar Medical College", "Gurucharan College"),
            "Nagaon" to listOf("Any", "Nowgong College", "Nagaon Commerce College", "Dhing College"),
            "Sivasagar" to listOf("Any", "Sibsagar College", "Sribhumi College"),
            "Golaghat" to listOf("Any", "Golaghat Commerce College", "Dergaon Kamal Dowerah College"),
            "Barpeta" to listOf("Any", "Barpeta College", "Barpeta Girls College"),
            "Nalbari" to listOf("Any", "Nalbari College", "Tihu College"),
            "Kokrajhar" to listOf("Any", "Kokrajhar Government College", "Bodoland University"),
            "Tinsukia" to listOf("Any", "Tinsukia College", "Sadiya College"),
            "Dhubri" to listOf("Any", "Bilasipara College", "Dhubri College"),
            "Bongaigaon" to listOf("Any", "Bongaigaon College", "Abhayapuri College"),
            "Goalpara" to listOf("Any", "Dudhnoi College", "Goalpara College"),
            "Lakhimpur" to listOf("Any", "North Lakhimpur College", "Dhakuakhana College"),
            "Darrang" to listOf("Any", "Mangaldai College", "Sipajhar College"),
            "Morigaon" to listOf("Any", "Morigaon College", "Jagiroad College"),
            "Karbi Anglong" to listOf("Any", "Diphu Government College", "Howraghat College"),
            "Hojai" to listOf("Any", "Hojai College", "Lanka Mahavidyalaya"),
            "Hailakandi" to listOf("Any", "Hailakandi College"),
            "Karimganj" to listOf("Any", "Karimganj College", "Ramkrishna Nagar College"),
            "Biswanath" to listOf("Any", "Biswanath College", "Behali College"),
            "Charaideo" to listOf("Any", "Sonari College"),
            "Chirang" to listOf("Any", "Chirang College", "Bengtol College"),
            "Dhemaji" to listOf("Any", "Dhemaji College"),
            "Dima Hasao" to listOf("Any", "Haflong Government College"),
            "Majuli" to listOf("Any", "Majuli College"),
            "Baksa" to listOf("Any", "Mushalpur College", "Tamulpur College"),
            "Udalguri" to listOf("Any", "Udalguri College", "Kalaigaon College"),
            "South Salmara-Mankachar" to listOf("Any", "Mankachar College"),
            "West Karbi Anglong" to listOf("Any", "Hamren College")
        )
        
        val districtAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, districts)
        spinnerDistrict.setAdapter(districtAdapter)
        
        // Initially set colleges for "Any" district
        var currentColleges = districtToColleges["Any"] ?: listOf("Any")
        val collegeAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, currentColleges.toMutableList())
        spinnerCollege.setAdapter(collegeAdapter)
        
        // Add district selection listener to update colleges dynamically
        spinnerDistrict.setOnItemClickListener { _, _, position, _ ->
            val selectedDistrict = districts[position]
            val collegesForDistrict = districtToColleges[selectedDistrict] ?: listOf("Any")
            
            // Update college dropdown
            val newCollegeAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, collegesForDistrict)
            spinnerCollege.setAdapter(newCollegeAdapter)
            spinnerCollege.setText("", false) // Clear selection
        }
        
        // Prefill district and college if previously selected
        filterDistrict?.let { 
            spinnerDistrict.setText(it, false)
            // Update colleges for the prefilled district
            val collegesForDistrict = districtToColleges[it] ?: listOf("Any")
            val newCollegeAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, collegesForDistrict)
            spinnerCollege.setAdapter(newCollegeAdapter)
        }
        filterCollege?.let { spinnerCollege.setText(it, false) }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<android.widget.Button>(R.id.buttonClearFilters).setOnClickListener {
            filterStatus = null
            filterMinPrice = null
            filterMaxPrice = null
            filterDate = "Newest first" // Reset to default instead of null
            filterDistrict = null
            filterCollege = null
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

            filterDistrict = spinnerDistrict.text.toString().trim().let { if (it == "Any" || it.isEmpty()) null else it }
            filterCollege = spinnerCollege.text.toString().trim().let { if (it == "Any" || it.isEmpty()) null else it }

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
                for (data in snapshot.children) {
                    val ad = data.getValue(Ad::class.java)
                    ad?.let { allAds.add(it) }
                }
                applyFilters()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun applyFilters(searchQuery: String = "") {
        synchronized(this) {
            android.util.Log.d("MainActivity", "applyFilters: selectedCategory = $selectedCategory")

            val query = binding.searchBar.text.toString()
            val lowercaseQuery = query.lowercase()

            filteredAds.clear()

            for (ad in allAds) {
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
                    val price = ad.value.toLongOrNull() ?: 0L
                    val minOk = filterMinPrice?.let { price >= it } ?: true
                    val maxOk = filterMaxPrice?.let { price <= it } ?: true
                    minOk && maxOk
                }

                val matchesDistrict = filterDistrict?.let { district ->
                    ad.district.equals(district, ignoreCase = true)
                } ?: true

                val matchesCollege = filterCollege?.let { college ->
                    ad.collegeName.equals(college, ignoreCase = true)
                } ?: true

                // Filter out ads posted by the current user
                val currentUserId = auth.currentUser?.uid
                val isNotOwnAd = if (currentUserId != null) {
                    ad.sellerId != currentUserId
                } else {
                    true // If not logged in, show all ads
                }

                if (matchesText && matchesCategory && matchesStatus && matchesPrice && matchesDistrict && matchesCollege && isNotOwnAd) {

                    filteredAds.add(ad)

                }

            }


            android.util.Log.d("MainActivity", "applyFilters: filteredAds.size = ${filteredAds.size}")

            adsAdapter.notifyDataSetChanged()

        }
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

