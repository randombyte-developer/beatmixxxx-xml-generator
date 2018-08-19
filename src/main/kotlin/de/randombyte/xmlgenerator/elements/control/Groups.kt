package de.randombyte.xmlgenerator.elements.control

object Groups {

    const val MASTER = "[Master]"
    const val VINYL_CONTROL = "[VinylControl]"
    const val RECORDING = "[Recording]"
    const val AUTO_DJ = "[AutoDJ]"
    const val LIBRARY = "[Library]"
    const val PLAYLIST = "[Playlist]"
    const val CONTROLS = "[Controls]"

    fun channel(n: Int) = "[Channel$n]"
    fun sampler(n: Int) = "[Sampler$n]"
    fun previewDeck(n: Int) = "[PreviewDeck$n]"
    fun microphone(n: Int) = "[Microphone$n]"

    fun effectRack(n: Int) = "[EffectRack$n]"
    fun effectUnit(rack: Int, n: Int) = "[EffectRack${rack}_EffectUnit$n]"
    fun effect(rack: Int, unit: Int, n: Int) = "[EffectRack${rack}_EffectUnit${unit}_Effect$n]"

    fun equalizerEffect(rack: Int, channel: Int, n: Int) = "[EqualizerRack${rack}_[Channel$channel]_Effect$n]"
    fun quickEffect(rack: Int, channel: Int) = "[QuickEffectRack${rack}_[Channel$channel]]"
}
