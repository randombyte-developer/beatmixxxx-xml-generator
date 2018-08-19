This is a Midi XML file mapping generator for [Mixxx](https://www.mixxx.org/). The only target controller is the
Reloop Beatmix 4. Mixxx has 3 X's, and the Beatmix supports 4 Decks, so Beatmi_xxxx_.

Mixxx mappings need an XML file and a JS file. Only those Midi signals which were defined in the XML file get through into
the script. Reloading the script can be done dynamically, the XML file not. The XML file requires massive duplication for all 4 decks. XML
files are only descriptive and ofc don't offer any real programming. So I want to extract as much as possible into JS.

To aid with creating a massive XML file, this project was created.

This idea is not new, nor by me. See https://www.mixxx.org/forums/viewtopic.php?f=7&t=11677