def resetComputers(String agentName = null) {
    println("resetting computer workspace inUse maps")
    jenkins.model.Jenkins.instance.getComputers().each{
        if(agentName && it.name == agentName){
            //Make sure the computer is idle...  _completely_ idle
            if(it.countBusy() <= 0){
                println("checking ${it.name} for stale inUse entries ...")
                // each computer has a workspaceList object. each workspaceList object
                // has a private inUse hashmap that keeps track of which workspaces are in
                // use in order to know when to create a new workspace with @2, @3, etc.
                // due to a bug, these don't always get cleared up.
                //
                // because the computer is idle, we know the inUse map should be empty.
                // we use reflection to check if it is empty, and if it is not, we
                // overwrite it with a new empty map. this is pretty hacky but the
                // alternative is to reboot jenkins.
                def workspaceList = it.getWorkspaceList()
                def field = workspaceList.getClass().getDeclaredField("inUse")
                println( workspaceList.getClass())
                field.setAccessible(true)
                def oldMap = (Map) field.get(workspaceList)
                if (!oldMap.isEmpty()) {
                    println("  --X found, resetting ${it.name}")
                    oldMap.each{key, value ->
                        println("    - [${key}] ${value}")
                    }
                    field.set(workspaceList, [:])
                } else {
                    println("  --> ${it.name} has no stale inUse entries")
                }
            }else{
                println("  --b ${it.name} is busy, skipping check")
            }
        }else{
             println("  --o ${it.name} skipped as it doe not match filter [${agentName}] ")
        }
    }
    println( "done resetting computer workspace inUse maps")
}

def checkLeases(String agentName = null) {
    println("Checking all leases to try and find bad ones")
    jenkins.model.Jenkins.instance.getComputers().each{
        def check = true
        if(agentName != null){
            if(it.name != agentName){
                check = false
            }
        }
        if(check){
            //Make sure the computer is idle...  _completely_ idle
            println("checking ${it.name} for stale inUse entries ...")
            // so it's the same algorithm as the clear lease with the exception that we dont check for idle jobs
            // idle machines or anything.
            // this basically lists all the leases presently in use without discrimination
            def workspaceList = it.getWorkspaceList()
            def field = workspaceList.getClass().getDeclaredField("inUse")
            println( workspaceList.getClass())
            field.setAccessible(true)
            def oldMap = (Map) field.get(workspaceList)
            if (!oldMap.isEmpty()) {
                println("  --X found, resetting ${it.name}")
                oldMap.each{key, value ->
                    println("    - [${key}] ${value} from thread ${value.holder.getName()}")
                    it.getAllExecutors().each{ ex -> 
                        println("      - ${ex.getName()}")
                    }
                }
                
            } else {
                println("  --> ${it.name} has no stale inUse entries")
            }
        }else{ 
             println("  --o ${it.name} skipped as it doe not match filter [${agentName}] ")
        }
    }
    println( "done resetting computer workspace inUse maps")
}
