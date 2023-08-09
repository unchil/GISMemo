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
}
```

| Tab      | Screen | Description     |
|  :----:         |    :----:   |          :----  |
| List   | <img src="https://github.com/unchil/GISMemo/blob/main/docu/resource/memomap.jpg"  width="600px" title="전체 맵" alt="테스트"></img>         |  It is a list screen of created memos, and features such as search, share, delete, and view details are provided.     |
| Write   | <img src="https://github.com/unchil/GISMemo/blob/main/docu/resource/memomap.jpg"  width="500px" title="전체 맵" alt="테스트"></img>         | This screen is for writing notes. It provides functions such as screenshots of pictures drawn on maps, texts using voice recognition, photos, and videos, and it is possible to set security, markers, and hashtags.     |
| Markers   | <img src="https://github.com/unchil/GISMemo/blob/main/docu/resource/memomap.jpg"  width="500px" title="전체 맵" alt="테스트"></img>         |  The location of the memo with the marker set is displayed.If you touch the information window of the marker, a brief screen of the memo is displayed, and if you touch the screen, you move to the detailed view screen.     |
| Setting   | Text        | And more      |

##  License
```
   Copyright 2022-present lincollincol

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

##  License
```
 The MIT License (MIT)

Copyright (c) <year> <copyright holders>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```



[googlelink]: https://developers.google.com/maps "Go GoogleMap"
[openweatherlink]: https://openweathermap.org/ "Go OpenWeatherMap"
[compose-audiowaveform_link]: https://github.com/lincollincol/compose-audiowaveform "Go compose-audiowaveform"
