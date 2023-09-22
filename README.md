# GISMemo


#### Kotlin Compose RoomDatabase Flow Paging Retrofit Coil Exoplayer SpeechRecognizer Camera GoogleMap OpenWeather

## Download

Production App: [GISMemo][release].

## Description

This GISMemo is a memo program based on Google Map: [Google Maps Platform][googlelink].

Weather information provided by OpenWeather: [OpenWeatherMap][openweatherlink]. 

Gis Memo App supports the use of haptics and night mode.

Available languages include Korean, English, French, and Portuguese.
 
Memos are basically saved as snapshots of weather information and maps at the time and location of the memo.

The main information of a memo consists of *voice translation text*, *photos*, *videos*, and *snapshots* of drawings drawn on the map.

Memos can be kept secret through *security* settings

By setting the *marker*, you can check the writing position of the memo in the overall map view.

Memos can be searched by *title*, *creation date*, *hashtag*, *security*, and *marker* conditions.

Memos can be shared via e-mail through the *sharing function*.


```
GoogleMap and OpenWeather api key required
 
local.properties 
{
  MAPS_API_KEY=
  OPENWEATHER_KEY=
}
```


###  Video
|Write|Detail|Map|Setting|
|:-:|:-:|:-:|:-:|
|[![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/write_portrait.png)](https://youtube.com/shorts/uM2O647Z7TE?feature=share)|[![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/detail_portrait.png)](https://youtube.com/shorts/5z5QSPmNzvQ?si=7DvLyimANKWn2uQ4)|[![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/map_portrait.png)](https://youtube.com/shorts/-yUi1thvrrw?feature=share)|[![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/setting_portrait.png)](https://youtube.com/shorts/SAq5fDb9DWo?feature=share)|


###  Screen Shot
|     |                                          Portrait                                          |                                                                                                                                                        Landscape                                                                                                                                                        |
|:---:|:------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|**List**|  ![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/list_portrait.png)  |                                                  ```It is a list screen of created memos, and features such as search, share, delete, and view details are provided.```![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/list_landscape.png)</img>                                                  |
|**Write**| ![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/write_portrait.png)  |  ```This screen is for writing notes. It provides functions such as screenshots of pictures drawn on maps, texts using voice recognition, photos, and videos, and it is possible to set security, markers, and hashtags.```![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/write_landscape.png)   |                                                             
|**Map**|  ![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/map_portrait.png)   | ```The location of the memo with the marker set is displayed.If you touch the information window of the marker, a brief screen of the memo is displayed, and if you touch the screen, you move to the detailed view screen.``` ![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/map_landscape.png) | 
|**Search**| ![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/search_portrait.png) |                                                                ```Memos can be searched by title, creation date, hashtag, security, and marker conditions.```![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/search_landscape.png)                                                                |   
|**Detail**| ![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/detail_portrait.png) |                               ```This is the screen that appears when you tap an item in the list or tap an item in the entire map. Can check the detailed information of the written memo.```![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/detail_landscape.png)                               |   
|**Setting**| ![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/setting_portrait.png) |                               ```This GISMEMO can set the following functions.  Haptic, night mode, dynamic color, delete all notes, language selection (Korean, English, French, Portuguese, Spanish, Chinese)```![Alt text](https://github.com/unchil/GISMemo/blob/main/app/src/main/assets/setting_landscape.png)        

##  License
**SPDX-License-Identifier: MIT**


[release]: https://play.google.com/store/apps/details?id=com.unchil.gismemo&pcampaignid=web_share "GIS MEMO"
[googlelink]: https://developers.google.com/maps "Go GoogleMap"
[openweatherlink]: https://openweathermap.org/ "Go OpenWeatherMap"
