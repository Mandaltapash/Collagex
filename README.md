<!-- # Title -->
# OLX Clone
![Demo](https://static.bn-static.com/img-49626/olx-share.jpg)


<!-- # Short Description -->

>- In this application, the user is able to see **ads** from all the members
>- The user can add new ads to the platform, setting all his information as *value*, *region*, *category*, *description*, *title* and *phone* for contact
>- Filter the ads by *region* and *category* to find closer contacts

This application was written in Kotlin language using Android Studio. It uses Google Firebase as database, saving ads and it's information and user information in there. 

<!-- # Badges -->
<div style="display: inline_block"><br>
    <img height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/androidstudio/androidstudio-original.svg">
    <img height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/firebase/firebase-plain.svg">
    <img height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kotlin/kotlin-original.svg">
</div>

---

# Tags

`Android Studio` `Firebase` `Kotlin` `OLX`

---


# Demo

![Demo](https://media.discordapp.net/attachments/655489748885831713/1042662254958608394/ezgif.com-gif-maker.gif)


>- Registration and Login using email and password authenticated by Google Firebase!


![Demo](https://media.discordapp.net/attachments/655489748885831713/1042664413813035008/ezgif.com-gif-maker-2.gif)


>- Create your personalised ad and set all the information you need for your sell!
>- Choose photos from your internal gallery to improve your ad!
>- Set up your phone number to receive direct calls and finish your sell!
  
![Demo](https://media.discordapp.net/attachments/655489748885831713/1042666827483988039/ezgif.com-gif-maker.gif)


>- Filter the **ads** by *region* and *category* to make your search easier!

![Demo](https://media.discordapp.net/attachments/655489748885831713/1042669770954256394/ezgif.com-gif-maker-2.gif)


>- Click in which **ad** you want to check for more information!
>- Call directly the registered number in the **ad** to make the communication easier!

---

# Code Example
```kotlin
private fun recoverAdsByRegion(){
        configDialog()
        adRef = FirebaseDatabase.getInstance()
            .getReference("ads")
            .child(filterRegion)
        adRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                adList.clear()
                for (category: DataSnapshot in snapshot.children){
                    for (ads: DataSnapshot in category.children){
                        val ad: Ad = ads.getValue(Ad::class.java)!!
                        adList.add(ad)
                    }
                }
                adList.reverse()
                adapterAd.notifyDataSetChanged()
                dialog.dismiss()
                binding.buttonRegion.text = filterRegion
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
            }

        })
    }
```

Example of the code needed to recover the **ads** from the **Google Firebase** to create the exhibition in the *RecyclerView*, filtering all the registered information by the selected *region*

---

# Libraries

>- [CarouselView](https://github.com/sayyam/carouselview)
>- [Picasso](https://github.com/square/picasso)
>- [SpotsDialog](https://github.com/dybarsky/spots-dialog)
>- [Maskara](https://github.com/santalu/maskara)
>- [CurrencyEditText](https://github.com/BlacKCaT27/CurrencyEditText)
>- [GoogleFirebase](https://firebase.google.com)

---

# Contributors

- [Thiago Rodrigues](https://www.linkedin.com/in/tods/)
