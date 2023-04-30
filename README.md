# Fossil voice test

Bridge for Fossil hybrid smartwatches to use voice commands in [GadgetBridge](https://gadgetbridge.org/).

How to use :

1. In GadgetBridge, go to the watch settings and into "Developer settings"
2. Set "Voice service package name" to `net.leloubil.fossilvoicetest`
3. Set "Voice service class name" to `net.leloubil.fossilvoicetest.VoiceDataReceiverService`
4. Configure your preferred language in the app
5. Use an automation app like Tasker to listen for the intent `net.leloubil.fossilvoicetest.VOICE`, the recognized text
   is in the `TEXT` extra
6. Open the Alexa app on your watch and say your command

Right now, there is a 2-second delay before sending the intent
after stopping recognition in order to make sure this app received all the audio data.

Uses [Vosk](https://alphacephei.com/vosk/) for speech recognition.
Model `vosk-model-small-fr-0.22` for french and `vosk-model-small-en-us-0.15` for english are included in the app.
