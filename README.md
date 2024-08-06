# esslli2024

Code for the incremental dialogue system demo with InproTK for he ESSLLI 2024 course on Real-time Language Processing in Conversational AI. The below are some instructions to get it running on your own machines.

Other Tutorial sources:
https://sourceforge.net/p/inprotk/wiki/Setup/
http://www.dsg-bielefeld.de/dsg_wp/intro-to-inprotk/

Instructions for Windows and Eclipse Version: 2019-06 (4.12.0). Where instructions different for LINUX/Mac, this is in square brackets.

1. Download Java jdk and Eclipse.
* Download the Eclipse IDE from https://www.eclipse.org/downloads/
* Before/during this installation, you may need to install a Java JDK (1.8+) before you start downloading.
* Download eclipse installer (as the eclipse installer .exe file [different file for LINUX users, which is a zipped folder]), both available at:
 
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html (1.8)

* Unzip the installer if needed and run the executable .exe file by clicking on it [LINUX: just run `./eclipse-inst` from inside the extracted installer directory downloaded from the Eclipse site] and follow the set-up.]
* Choose a workspace/use default workspace for Eclipse.

2. Get the dependent repos
* git clone the following two repositories from bitbucket onto your local machine, as sister folders to this local repo:

https://github.com/Dynamics-of-Language/dylan
https://github.com/Dynamics-of-Language/dylan_util


3. Import this project into Eclipse and build it.
* Import the IncrementalDialogue project into Eclipse by either selecting select File->Import->General->Existing Projects into Workspace, navigate to the IncrementalDialogue folder and hit "Finish", or by File>Import>Git>Projects from Git then select the top level of this entire repository folder. In Eclipse in your package explorer you should see the project 'IncrementalDialogue' appear in the package manager.

* Import the `dylan` projects and `dylan_util` repos as existing projects into Eclipse in the same way. It is best to configure the build path to include those two projects. To do that, right-/ctrl click on the IncrementalDialogue project in the package tab, and  Build Path -> Configure Build Path -> Projects (select Dylan and dylan_util).

* Make sure there are no errors (denoted by red marks) by expanding the packages in your Package Explorer browser, particularly the src/ folder. You can click on the "Problems" tab to see where the problems are. Make sure all the required libraries are included in the project's build path. To do that, right-/ctrl click on Project in the package tab, and  Build Path -> Configure Build Path -> Libraries (select jar files in the lib folder). 


* Note to run the ASR demos, you will also have to ensure that your operating system is allowing eclipse access to your microphone - sometimes this is not ensured by default.

* To run the Google ASR demo, you need to have authentication to do from the Google Cloud Speech API. You can generate an API key for a project using speech and paste that key as they key1 value at line 105 at IncrementalDialogue/src/app/DialogueSystemGoogleASR.java, replacing "your_api_key_here" with your Google Cloud API key:

String key1 = "your_api_key_here"; 


4. Try running the Google ASR based numbers application from the microphone:
* Go to ```IncrementalDialogue/src/app/DialogueSystemGoogleASR.java```
* Run -> Run As -> Java Application
It can take up to 30 seconds before the system is ready to respond, so have a cup of tea until you see a message which includes a message which looks something like 'microphone started at 1655821226730' (where the long number could be another long number). Try talking into your microphone and see what results come out from the console.  You can also type into the interface. You should see the ADD, COMMIT and REVOKE messages with the associated word hypothesis IUs being printed to your console.

* (Alternative to whole of step 4) If you are having problems with the ASR input, you can try a simple text input system by running ```src/app/DialogueSystemTypedInput.java```


5. Change the module architecture with ```iu_config.xml```
* Look at ```src/config/iu_config.xml``` file. That is where the architecture is determined. Try to work out what is going on.
* To experiment with modifying the output buffer of the ASR module (and therefore the configuration of the whole system's network) can make a simpler app than those tried above which simply prints out the current ASR hypotheses to the terminal, by commenting out all connecting modules from the "currentASRHypothesis" module's hypChangeListeners list to its right buffer, i.e. set it to:

```
     <component name="currentASRHypothesis" type="inpro.incremental.source.SphinxASR"> 
        <property name="frontend" value="${frontend}" />
        <property name="asrFilter" value="${deltifier}" />
        <property name="brutalPruner" value="trivialPruner"/>
        <property name="baseData" value="${baseData}" />
        <propertylist name="hypChangeListeners">
        	<item>printer</item>
        	<!--<item>ASRanalyzer</item>-->
        	<!--<item>NLU</item>-->
        </propertylist>
    </component> 
```
        
* Run DialogueSystemGoogleASR.java again to see the difference.
        	
6. Change the module architecture with iu_config.xml to try the DyLan DS-TTR parser from speech
* Modify Try the Dylan parser for a simple robot domain by commenting out the NLU module as it is and uncommenting the Dylan parser NLU module. Make sure you haven't commented out the link to NLU from the currentASRHypothesis/uncomment from step 5, so you should have the below:

```    
     <!--A simple DS-TTR parsing module which just parses every word-->
     <property name="grammar" value="2015-english-ttr-robot"/> <!-- name of the DS-TTR folder -->
  
     <component name="NLU" type="module.nlu.NLUparserDSTTR">
   	<property name="grammar" value="${grammar}"/>
   	<property name="language" value="${language}"/>
        <propertylist name="hypChangeListeners">
        </propertylist>
    </component>

    <!--
    <component name="NLU" type="module.nlu.NLUnumbers">
   	<property name="grammar" value="${grammar}"/>
   	<property name="language" value="${language}"/>
        <propertylist name="hypChangeListeners">
        <item>DM</item> 
        </propertylist>
    </component>
     -->
 ```

* Run ```DialogueSystemGoogleASR.java``` again to see the difference. You should see the DyLan parser interface showing incremental updates as you speak into the mic.
