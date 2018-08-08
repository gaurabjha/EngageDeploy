Param(
 [switch]$byPatchgroup,
 [switch]$byDCID,
 [switch]$3Step,
 [string]$patchGroup,
 [string[]]$DCID,
 [Parameter(Mandatory=$True)]
 [string]$deploymentType,
 #[Parameter(Mandatory=$True)]
 [string]$currentLucasSoftware,
 #[Parameter(Mandatory=$True)]
 [string]$newLucasSoftware,
 [string]$clusterTarget,
 [string]$status
)
$scriptHome="C:\Lucas_Tools\EngageDeployment\"
$statusHome=$scriptHome + "Status"
$scriptname="EngageDeploy"
$interval=5
$logHome = $scriptHome + "Logs\"
$LogFileName=$logHome + $scriptname+"_"+ $( Get-Date -Format "yyyy-MM-dd_HHmmss")+"_${status}_log.txt"

$config_xml = [xml](get-content "${scriptHome}engage_deployment_config.xml")
$mailto = $config_xml.PATCHING.EMAIL_LIST.EMAIL_ID

#Log function
function log ($string){
    $log_string = "$(Get-Date) == $string"
    Write-Host $log_string
    add-content -Path $LogFileName -Value $log_string -Force
}

#Mail and Logging function with exit command
function sendMail($errormessage){
    log $errormessage
    $messageBody = "Script terminated with errors, please see log file"
    $taskStatus = $status + " - FAILED"
	if($errormessage -eq "[INFO] Deployment Completed Successfully"){
        $messageBody = $errormessage
        $taskStatus = $status + " - SUCCESSFUL"
    }
    Send-MailMessage -Body "$messageBody, please see log file" -Attachments $LogFileName -From $scriptname@owens-minor.com -Subject "$scriptname - $taskStatus" -To $mailto -SmtpServer relay.owens-minor.com  
    #Remove-Item –path $LogFileName
	$statusFile = $statusHome + "\" + $status
	write-host "Deleting status file " $statusFile
	Remove-Item –path $statusFile
    exit
}

#Current Step Intimation
function sendInfo($paramMessage){
    log("[INFO] " + $paramMessage)
    Send-MailMessage -From $scriptname@owens-minor.com -Subject "$scriptname - $paramMessage" -To $mailto -SmtpServer relay.owens-minor.com 
}

#Service Disable
function disableService($paramDCID, $paramServer){
    try{
        $var_service_name="Lucas.Directory.Monitor-$paramDCID","Lucas.Export.Service-$paramDCID","Lucas.Import.Service-$paramDCID","Lucas.NextJen.Server-$paramDCID","Lucas.Notification.Service-$paramDCID","Lucas.Transaction.Server.Service-$paramDCID"
        #Write-Host "Disabling the Services";
        foreach($currentService in $var_service_name){
            Write-Host "Stopping Services : " $currentService  " on Server "  $paramServer

		    if(Test-Connection $paramServer)
		    {
			    #log "[DEBUG] Validated connection to $paramServer, moving forward"
                
                #Change The Recovery Option
                $serverPath = "\\" + $paramServer
		        sc.exe $serverPath failure $currentService actions= ////// reset= 0

                #Stop the Service
			    $ProcessID=get-wmiobject win32_service  -ComputerName $paramServer | where { $_.name -eq $currentService }
                Invoke-Command -ComputerName $paramServer -ScriptBlock {
                    param($paramProcessID )
                    Stop-Process -ID $paramProcessID -Force
                } -ArgumentList $ProcessID.ProcessId

                #Validation
			    Start-Sleep $interval
			    $current = Get-Service -Name $currentService -ComputerName $paramServer

			    if($current.Status -match "Stopped")
			    {
				    log "[INFO] Successfully stopped $currentService service on $paramServer"
			    }
			    else
			    {
				    sendMail("[ERR] $currentService did not stop in $interval seconds")
			    }
			    clear-variable current
		    }
		    else
		    {
			    sendMail("[ERR] unable to connect to $paramServer to start validation") 
		    }
	    }
        sendInfo ("All Services Stopped for DC : "+ $paramDCID)
    }catch [Exception]
    {
        sendMail("[FATAL] Exception Occured, Exception Name "+$_.Exception.GetType().FullName +" Exception Message :" + $_.Exception.Message )
    }
}

#Service Enable
function enableService($paramDCID, $paramServer){
    try{
        $var_service_name="Lucas.Directory.Monitor-$paramDCID","Lucas.Export.Service-$paramDCID","Lucas.Import.Service-$paramDCID","Lucas.NextJen.Server-$paramDCID","Lucas.Notification.Service-$paramDCID","Lucas.Transaction.Server.Service-$paramDCID"
        #Write-Host "Enabling the Services";
        foreach($currentService in $var_service_name){
            Write-Host "Starting Services : " $currentService  " on Server "  $paramServer

		    if(Test-Connection $paramServer)
		    {
			    #log "[DEBUG] Validated connection to $paramServer, moving forward"
			    Get-Service -Name $currentService -ComputerName $paramServer | Start-Service -Force
			    Start-Sleep $interval
			    $current = Get-Service -Name $currentService -ComputerName $paramServer

			    if($current.Status -match "Running")
			    {
				    log "[INFO] Successfully Started $currentService service on $paramServer"
			    }
			    else
			    {
				    sendMail("[ERR] $currentService did not Start in $interval seconds")
			    }
			    clear-variable current
		    }
		    else
		    {
			    sendMail("[ERR] Unable to connect to $paramServer to start validation")
            }
	    }
        sendInfo ("All Services Started for DC : "+ $paramDCID)
    }catch [Exception]
    {
        sendMail("[FATAL] Exception Occured, Exception Name "+$_.Exception.GetType().FullName +" Exception Message :" + $_.Exception.Message )
    }
}

#Running Lucas Services
function getRunningLucasService($paramDCID , $paramServer){
    Start-Sleep 10
    $count=0;
    $var_service_name="Lucas.Directory.Monitor-$paramDCID","Lucas.Export.Service-$paramDCID","Lucas.Import.Service-$paramDCID","Lucas.NextJen.Server-$paramDCID","Lucas.Notification.Service-$paramDCID","Lucas.Transaction.Server.Service-$paramDCID"
    foreach($currentService in $var_service_name){
        try{
		    if(Test-Connection $paramServer)
		    {
			    $current = Get-Service -Name $currentService -ComputerName $paramServer -ErrorAction SilentlyContinue
                if($current){
			        if($current.Status -match "Running")
			        {
                        log "Service Installed : " $currentService 
				        $count = $count+1;
			        }else{
                        log "Service Exists, but not Started : " $currentService 
                    }
                }else{
                    log "Service Removed : ${currentService}" 
                    $count = $count-1;
                } 
		    }else
		    {
			    sendMail("[ERR] Unable to connect to $paramServer to start validation") 
		    }
        }catch [Exception] {
            $count=$count-1;
        }
	}
    
    if($count -eq $var_service_name.Count){
        return 1; #All Services are running
    }
    if(($count * -1) -eq $var_service_name.Count){
        return 2; #All services are missing
    }


    #Positive Number gives the Number of Services Running
    #Negative Number Means the number of services missing

    log("[DEBUG] Service Cound = " + $count ) 
    return 0; #All services are stopped or partially stopped or partially running

}


function  packageValidate($val_paramDCID, $val_paramServer, $val_software ){
    #Returns True if package missings
    
    if( Test-Path "\\${val_paramServer}\C$\Lucas_Setup-${val_paramDCID}\${val_software}" ){
        Write-Host "Package Found"
    }else{
        sendMail("[ERR] ${val_software} not found for DC " + $val_paramDCID + " on " + $val_paramServer )
    }

}

#Uninstall the software $currentLucasSoftware 
function uninstallLucas($paramDCID , $paramServer){

    #Write-Host "Uninstalling Engage";

    if(Test-Connection $paramServer)
    {
        if($paramDCID -in $config_xml.PATCHING.NON_STANDARD_DIRECTORY.DC.DCID){
            $lucasHome = ($config_xml.PATCHING.NON_STANDARD_DIRECTORY.DC | where{ $_.DCID -eq $paramDCID} ).DIRECTORY
        }else{    
            $lucasHome = "C:\Lucas_Setup-"+${paramDCID}
        }

        Write-Host "Unistalling  $currentLucasSoftware DC ${paramDCID} on ${paramServer} "
        Invoke-Command -ComputerName $paramServer -ScriptBlock { 
            param($lucasHome , $currentLucasSoftware)
            Write-Host "Start-Process C:\Windows\System32\cmd.exe -ArgumentList /c cd $lucasHome & $currentLucasSoftware -u -Wait"
            Start-Process C:\Windows\System32\cmd.exe -ArgumentList "/c cd $lucasHome & $currentLucasSoftware -u" -Wait
        } -ArgumentList "$lucasHome","$currentLucasSoftware"
        
        Write-Host "Execution of Uninstall Complete, Verifying....."
        
        #validation for the services
        $lucasServicesMissing = 0;
        $lucasServicesMissing = getRunningLucasService $paramDCID $paramServer
    
        if ($lucasServicesMissing -eq 2)
        {
            Write-Host "UnInstalled $newLucasSoftware DC $paramDCID"
            sendInfo ("UnInstalled $newLucasSoftware  DC $paramDCID ")
        }else{
            Write-Host "[ERR] Unistallation Failed $newLucasSoftware DC $paramDCID"
            sendMail("[ERR] Unistallation Failed $newLucasSoftware DC $paramDCID")
        }

    }else{
		sendMail("[ERR] Unable to connect DC ${paramDCID}@${paramServer} to Uninstall") 
    }

}

#Install The Software 
function installLucas($paramDCID, $paramServer, $software){

    Write-Host "Installing Engage";

    if(Test-Connection $paramServer)
    {
        if($paramDCID -in $config_xml.PATCHING.NON_STANDARD_DIRECTORY.DC.DCID){
            $lucasHome = ($config_xml.PATCHING.NON_STANDARD_DIRECTORY.DC | where{ $_.DCID -eq $paramDCID} ).DIRECTORY
        }else{    
            $lucasHome = "C:\Lucas_Setup-"+${paramDCID}
        }

       Write-Host "Installing $newLucasSoftware DC ${paramDCID} on ${paramServer} "

        Invoke-Command -ComputerName $paramServer -ScriptBlock { 
            param($lucasHome , $software)
            Write-Host "Start-Process C:\Windows\System32\cmd.exe -ArgumentList /c cd $lucasHome & $software -i -Wait"
            Start-Process C:\Windows\System32\cmd.exe -ArgumentList "/c cd $lucasHome & $software -i" -Wait
        } -ArgumentList "$lucasHome","$software"

        Write-Host "Execution of Install Complete, Verifying....."

        #validation for the services
        $servicesRunning = 0;
        $servicesRunning = getRunningLucasService $paramDCID $paramServer

        if ($servicesRunning -eq 1)
        {
            Write-Host "Installed $software DC $paramDCID"
            sendInfo ("Installed $software  DC $paramDCID ")
        }else{
            Write-Host "[ERR] Installation Failed $software DC $paramDCID"
            sendMail("[ERR] Installation Failed $software DC $paramDCID")
        }

        # moving old executable to archive
        if($software -eq $newLucasSoftware){
            Invoke-Command -ComputerName $paramServer -ScriptBlock { 
                param($lucasHome , $software)
                Move-Item  -path "${lucasHome}\$software"  -destination "${lucasHome}\archive\$software"
            } -ArgumentList "$lucasHome","$currentLucasSoftware"  
            log "[INFO] Archived package : ${software}"
        }
    }else{
		sendMail("[ERR] unable to connect DC ${paramDCID}@${paramServer} to Install") 
    }
}

#Function for Standalone Clustered Servers
function deployCluster([Object]$paramDC){

    Write-Host "[DEBUG] I'm in Cluster Deploy for DC " $paramDC.DCID

    #Fetch the Owener Node
    
    if($clusterTarget.Length > 0){
        $serverForOwnerTest= $clusterTarget
    }else{
        $serverForOwnerTest=$paramDC.ServerName[0]     
    }

    $ownerNode=Invoke-Command -ComputerName $serverForOwnerTest -ScriptBlock {import-module FailoverClusters; $ClusterService=$args[0];(Get-ClusterGroup  $ClusterService).OwnerNode.Name} -argumentlist $paramDC.ClusterService

    #Get nonOwnerNode
    $nonOwnerNode
    $ownerNode = $ownerNode.Trim().ToUpper()
    if(  $ownerNode -eq $paramDC.ServerName[0].Trim().ToUpper() ){
        $nonOwnerNode =$paramDC.ServerName[1];
    }
    else{
        $nonOwnerNode =$paramDC.ServerName[0]; 
    }
    
    Write-Host "Owner Node Is : " $ownerNode
    Write-Host "Non - Owner Node Is : " $nonOwnerNode

    if($deploymentType -eq "F"){
        #Validate the Packages
        packageValidate  $paramDC.DCID $paramDC.ServerName $currentLucasSoftware
        packageValidate  $paramDC.DCID $paramDC.ServerName $newLucasSoftware


        #Stop Lucas Services on Onwer
        sendInfo ("Stopping Service | Owner : $ownerNode | DC : $paramDC.DCID")
        disableService $paramDC.DCID $ownerNode

        #Uninstall Lucas on non - Owner
        sendInfo ("UnInstalling $currentLucasSoftware | Non Owner : $nonOwnerNode | DC : $paramDC.DCID")
        uninstallLucas $paramDC.DCID $nonOwnerNode

        #Install Engage on non Owner
        sendInfo ("Installing $newLucasSoftware | Non Owner : $nonOwnerNode | DC : $paramDC.DCID")
        installLucas $paramDC.DCID $nonOwnerNode $newLucasSoftware

        #Stop Services on non Owner
        sendInfo ("Stopping Service | Non Owner : $nonOwnerNode | DC : $paramDC.DCID")
        disableService $paramDC.DCID $nonOwnerNode

        #Uninstall Engage on Owner
        sendInfo ("UnInstalling $currentLucasSoftware | Owner : $ownerNode | DC : $paramDC.DCID")
        uninstallLucas $paramDC.DCID $ownerNode

        #Install Engage on Owner
        sendInfo ("Installing $newLucasSoftware | Owner : $ownerNode | DC : $paramDC.DCID")
        installLucas $paramDC.DCID $ownerNode $newLucasSoftware
    
    }ElseIf($deploymentType -eq "U"){
        

        if($clusterTarget.Length > 0){ #if clusterTargeted

            
            if($clusterTarget.Trim().ToUpper() -eq $ownerNode.Trim().ToUpper() ){ #if owner is selected


                #Validate the Packages on owner
                packageValidate  $paramDC.DCID $ownerNode $currentLucasSoftware

                #Stop Lucas Services on Onwer
                sendInfo ("Stopping Service - Owner Cluser : $ownerNode DC : $paramDC.DCID")
                disableService $paramDC.DCID $ownerNode

                #Uninstall Lucas on non - Owner
                sendInfo ("UnInstalling $currentLucasSoftware - Owner : $ownerNode DC : $paramDC.DCID")
                uninstallLucas $paramDC.DCID $ownerNode
            
            }elseif($clusterTarget.Trim().ToUpper() -eq $nonOwnerNode.Trim().ToUpper() ){ #if non owner is selected
                
                #Validate the Packages on non owner
                packageValidate  $paramDC.DCID $nonOwnerNode $currentLucasSoftware

                #Stop Services on non Owner
                sendInfo ("Stopping Service | Non Owner : $nonOwnerNode | DC : $paramDC.DCID")
                disableService $paramDC.DCID $nonOwnerNode

                #Uninstall Engage on Owner
                sendInfo ("UnInstalling $currentLucasSoftware - Owner : $ownerNode DC : $paramDC.DCID")
                uninstallLucas $paramDC.DCID $nonOwnerNode


            }else{
                sendMail("[ERR] ${clusterTarget} not found" )
            }


        }else{ # if cluster not targeted

            
            #Validate the Packages on owner
            packageValidate  $paramDC.DCID $ownerNode $currentLucasSoftware
            #Validate the Packages on non owner
            packageValidate  $paramDC.DCID $nonOwnerNode $currentLucasSoftware

            #Stop Lucas Services on Onwer
            sendInfo ("Stopping Service - Owner Cluser : $ownerNode DC : $paramDC.DCID")
            disableService $paramDC.DCID $ownerNode

            #Uninstall Engage on Owner
            sendInfo ("UnInstalling $currentLucasSoftware - Owner : $ownerNode DC : $paramDC.DCID")
            uninstallLucas $paramDC.DCID $ownerNode

            #Uninstall Lucas on non - Owner
            sendInfo ("UnInstalling $currentLucasSoftware - Non Owner : $nonOwnerNode DC : $paramDC.DCID")
            uninstallLucas $paramDC.DCID $nonOwnerNode


        }
    }ElseIf($deploymentType -eq "I"){
        if($clusterTarget.Length > 0){

            if($clusterTarget.Trim().ToUpper() -eq $ownerNode.Trim().ToUpper() ){            
                
                #Validate the Packages on owner
                packageValidate  $paramDC.DCID $ownerNode $newLucasSoftware

                #Install Engage on Owner
                sendInfo ("Installing $newLucasSoftware - Owner : $ownerNode DC : $paramDC.DCID")
                installLucas $paramDC.DCID $ownerNode $newLucasSoftware

            }elseif($clusterTarget.Trim().ToUpper() -eq $nonOwnerNode.Trim().ToUpper() ){

                #Validate the Packages on non owner
                packageValidate  $paramDC.DCID $nonOwnerNode $newLucasSoftware

                #Stop Services on Owner
                sendInfo ("Stopping Service - Owner : $ownerNode DC : $paramDC.DCID")
                disableService $paramDC.DCID $ownerNode

                #Install Engage on non Owner
                sendInfo ("Installing $newLucasSoftware - Non Owner : $nonOwnerNode DC : $paramDC.DCID")
                installLucas $paramDC.DCID $nonOwnerNode $newLucasSoftware

                 #Stop Services on non Owner
                sendInfo ("Stopping Service - Non Owner : $nonOwnerNode DC : $paramDC.DCID")
                disableService $paramDC.DCID $nonOwnerNode

                #Start Services on Owner
                sendInfo ("Enabling Service -  Owner : $ownerNode DC : $paramDC.DCID")
                enableService $paramDC.DCID $ownerNode

            }else{
                sendMail("[ERR] ${clusterTarget} not found" )
            }
        }else{

            #Validate the Packages on owner
            packageValidate  $paramDC.DCID $ownerNode $newLucasSoftware
            packageValidate  $paramDC.DCID $nonOwnerNode $newLucasSoftware



            #Install Engage on non Owner
            sendInfo ("Installing $newLucasSoftware - Non Owner : $nonOwnerNode DC : $paramDC.DCID")
            installLucas $paramDC.DCID $nonOwnerNode $newLucasSoftware

            #Stop Services on non Owner
            sendInfo ("Stopping Service - Non Owner : $nonOwnerNode DC : $paramDC.DCID")
            disableService $paramDC.DCID $nonOwnerNode

            #Install Engage on Owner
            sendInfo ("Installing $newLucasSoftware - Owner : $ownerNode DC : $paramDC.DCID")
            installLucas $paramDC.DCID $ownerNode $newLucasSoftware

        }
    }
    #Done
}



function deployConsolidated([Object]$paramDC){
    Write-Host "[DEBUG] I'm in Consolidated Deploy for DC " $paramDC.DCID
    if($deploymentType -eq "F"){

        #Validate Packages
        #validate Current Pakage
        #Validate New Package
        packageValidate  $paramDC.DCID $paramDC.ServerName $currentLucasSoftwar
        packageValidate  $paramDC.DCID $paramDC.ServerName $newLucasSoftware

        if($3Step){

            sendInfo ("Stopping Service DC : "+ $paramDC.DCID)
            disableService $paramDC.DCID $paramDC.ServerName

            sendInfo ("Re-Installing $currentLucasSoftware DC : "+ $paramDC.DCID)
            installLucas $paramDC.DCID $paramDC.ServerName $currentLucasSoftware
        }

        sendInfo ("Stopping Service DC : "+ $paramDC.DCID)
        disableService $paramDC.DCID $paramDC.ServerName

        sendInfo ("UnInstalling $currentLucasSoftware DC : "+ $paramDC.DCID)
        uninstallLucas $paramDC.DCID $paramDC.ServerName

        sendInfo ("Installing $newLucasSoftware DC : "+ $paramDC.DCID)
        installLucas $paramDC.DCID $paramDC.ServerName $newLucasSoftware

    }ElseIf($deploymentType -eq "U"){
        
        #validate the Package exists
        
        packageValidate  $paramDC.DCID $paramDC.ServerName $currentLucasSoftware


        if($3Step){

            sendInfo ("Stopping Service DC : "+ $paramDC.DCID)
            disableService $paramDC.DCID $paramDC.ServerName

            sendInfo ("Re-Installing $currentLucasSoftware DC : "+ $paramDC.DCID)
            installLucas $paramDC.DCID $paramDC.ServerName $currentLucasSoftware
        }

        sendInfo ("Stopping Service DC : "+ $paramDC.DCID)
        disableService $paramDC.DCID $paramDC.ServerName

        sendInfo ("UnInstalling $currentLucasSoftware DC : "+ $paramDC.DCID)
        uninstallLucas $paramDC.DCID $paramDC.ServerName

    }ElseIf($deploymentType -eq "I"){

        #validate the Package exists
        packageValidate  $paramDC.DCID $paramDC.ServerName $newLucasSoftware


        sendInfo ("Installing $newLucasSoftware DC : "+ $paramDC.DCID)
        installLucas $paramDC.DCID $paramDC.ServerName $newLucasSoftware

    }
}

#Control Node Loop for the Activity Selected
function deployLucas([Object]$paramDC){
    try{

        $scriptname = "EngageDeploy" + $paramDC.DCID
        #Write-Host "current DC from deployLucas " $paramDC.DCID
        
        
        if($paramDC.ServerName.Count -gt 1){
            deployCluster $paramDC
        }else{
            deployConsolidated $paramDC
        }
    }catch [Exception]
    {
        sendMail("[FATAL] Exception Occured, Exception Name "+$_.Exception.GetType().FullName +" Exception Message :" + $_.Exception.Message )
    }
}

#Entry Point
function main(){
    try{
        #loading the XML Configuration Files 
        #The xml file should be in the same directory as the executable/script

        If($byPatchgroup -and $patchGroup -ne [string]::Empty )
        {
            log("Will do the activity PatchGroup ID : " + $patchGroup)
           
            [Object]$var_selectedPatchGroup = $config_xml.PATCHING.PATCHGROUP |  where { $_.PATCHGROUPID -eq $patchGroup }
            $All_DC=$var_selectedPatchGroup.DC
            
            if($All_DC){
                
                foreach($deployDC in $All_DC){
                    #Deploy the Activity for the selected DCs
                    log("[INFO] Begin Activity  - DC : "+$deployDC.DCID)
                    deployLucas $deployDC
                    log("[INFO] End Activity  - DC : "+$deployDC.DCID)
                    log("=============================================================================================================")
                }
            }
            else{
                sendMail("[ERR] No PatchGroup Found ");
            }
        }ElseIf($byDCID -and $DCID -ne [string]::Empty ){
            [string[]]$paramDCID = [string[]]($DCID -split ',')
            foreach($currentDCID in  $paramDCID){
                
                $deployDC = $config_xml.PATCHING.PATCHGROUP.DC | Where {$_.DCID -eq $currentDCID}
                
                if($deployDC){

                    log("[INFO] Begin Activity  - DC : "+$deployDC.DCID)
                    deployLucas $deployDC
                    log("[INFO] End Activity  - DC : "+$deployDC.DCID)
                }
                else{
                    sendInfo("[WARN] " + $currentDCID + " DC Not Found")
                }
           } 
        }Else{
            sendMail("[ERR] Invalid Arguement passed to the script")
        }
        sendMail("[INFO] Deployment Completed Successfully")
    }catch [Exception] {
        sendMail("[FATAL] Exception Occured, Exception Name "+$_.Exception.GetType().FullName +" Exception Message :" + $_.Exception.Message )
    }
}

main