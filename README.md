# GISMemo


#### Kotlin Compose RoomDatabase Flow Paging3 Retrofit Coil Exoplayer SpeechRecognizer Camera GoogleMap OpenWeather

## Description

This GISMemo is a memo program based on Google Map: [Google Maps Platform][googlelink].

Weather information provided by OpenWeather: [OpenWeatherMap][openweatherlink]. 

The audio amplitude used with exoplayer is the Compose AudioWaveformlibrary: [compose-audiowaveform][compose-audiowaveform_link] .
 
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

```
Add AudioWaveformlibrary

build.gradle(:app)
dependencies {
  implementation project(':audiowaveform')
  implementation 'com.github.lincollincol:amplituda:2.2.2'
  implementation 'com.github.lincollincol:compose-audiowaveform:1.1.1'
}
```



|     |     |    |
|:---:|:---:|:---:|
|**List**|![Alt text](https://github.com/unchil/GISMemo/blob/main/docu/resource/gismemo_list_portrait.jpg)|```It is a list screen of created memos, and features such as search, share, delete, and view details are provided.```![Alt text](https://github.com/unchil/GISMemo/blob/main/docu/resource/gismemo_list_landscape.jpg)</img>|
|**Write**|![Alt text](https://github.com/unchil/GISMemo/blob/main/docu/resource/gismemo_write_portrait.jpg)|```This screen is for writing notes. It provides functions such as screenshots of pictures drawn on maps, texts using voice recognition, photos, and videos, and it is possible to set security, markers, and hashtags.```![Alt text](https://github.com/unchil/GISMemo/blob/main/docu/resource/gismemo_write_landscape.jpg)|                                                             
|**Markers**|![Alt text](https://github.com/unchil/GISMemo/blob/main/docu/resource/gismemo_map_portrait.jpg)|```The location of the memo with the marker set is displayed.If you touch the information window of the marker, a brief screen of the memo is displayed, and if you touch the screen, you move to the detailed view screen.``` ![Alt text](https://github.com/unchil/GISMemo/blob/main/docu/resource/gismemo_map_landscape.jpg)| 
|**Search**|![Alt text](https://github.com/unchil/GISMemo/blob/main/docu/resource/gismemo_list_search_portrait.jpg)|```Memos can be searched by title, creation date, hashtag, security, and marker conditions.```![Alt text](https://github.com/unchil/GISMemo/blob/main/docu/resource/gismemo_list_search_landscape.jpg)|                                                                                                                                                                                                     


##  License
**SPDX-License-Identifier: MIT**



[googlelink]: https://developers.google.com/maps "Go GoogleMap"
[openweatherlink]: https://openweathermap.org/ "Go OpenWeatherMap"
[compose-audiowaveform_link]: https://github.com/lincollincol/compose-audiowaveform "Go compose-audiowaveform"
