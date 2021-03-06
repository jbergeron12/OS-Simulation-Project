// Imports (libraries and utilities)
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.Set;

// External imports
import com.google.common.collect.*;
import com.sun.istack.internal.Nullable;

// Event Manager class
public class EventManager {
	// Declare properties
	private ArrayListMultimap<String, Process> systemStates;
	private Map<String, Integer> stateLimits;

	// Constructor
	public EventManager() {
		// Let's initialize the Event Manager
		this.buildStateMap();

		// Let's initialize the state limits map
		this.buildStateLimitsMap();
	}

	// Private function to build the system state array/multimap
	private void buildStateMap() {
		// First of all, let's instanciate a multimap
		this.systemStates = ArrayListMultimap.create();
	}

	// Private function to build the system state limit map
	private void buildStateLimitsMap() {
		// First of all, let's instanciate a hashmap
		this.stateLimits = Maps.newHashMap();

		// Let's add our state limits
		this.stateLimits.put("Ready", 4);
		this.stateLimits.put("Blocked", 6);
		this.stateLimits.put("Run", 1);
	}

	// Private function to detect if the system state is full
	public boolean isStateFull(String state) {
		// Let's first see if the given state even has a limit
		if (this.stateLimits.containsKey(state)) {
			// Ok, the key must have a limit if it got here, so let's check to see if that limit's been reached
			// Get the states limit
			int stateLimit = this.stateLimits.get(state);

			// Get a count of the number of times a key appears in that set
			int stateProcessCount = this.getProcessCount(state);

			// If the number of processes in that state are at the limit
			if (stateProcessCount == stateLimit) {
				// Return true. The state is full
				return true;
			}
		}

		return false;
	}

	// Public function to detect if the system state is empty
	public boolean isStateEmpty(String state) {
		// Let's grab a list of all of the current processes in the given state
		List<Process> processes = this.getProcesses(state);

		// Let's return the lists isEmpty boolean value
		return processes.isEmpty();
	}

	// Public function to detect if adding the process to the given state is possible
	public boolean isAddPossible(Process process, String state) {
		// Let's first check if the state we're trying to put this process in is full
		if (this.isStateFull(state) != true) {
			// If we got here, the process can be successfully added
			return true;
		}

		return false;
	}

	// Public function to add processes to the event manager
	public boolean addProcess(Process process, String initialState) {
		// Let's first check if the state we're trying to put this process in is full
		if (this.isAddPossible(process, initialState)) {
			// Let's add the process to the state map
			if (this.systemStates.put(initialState, process)) {
				// If we got here, the process has successfully been added to the state manager
				return true;
			}
		}

		return false;
	}

	// Private function to remove a process from a particular state
	private boolean removeProcessFromState(Process process, String state) {
		// Let's simply return the boolean value of the remove operation
		return this.systemStates.remove(state, process);
	}

	// Public function to get all the processes in a given state
	public List<Process> getProcesses(String state) {
		// Get a list of processes in the given state
		return this.systemStates.get(state);
	}

	// Public function to get a process at a specific given index in a given state
	@Nullable public Process getProcessAtIndex(String state, int index) {
		// Let's create a process to be returned
		Process process = null; // Process may be null. We may not get back a process

		// Get a list of processes in the given state
		List<Process> processes = this.getProcesses(state);

		// Let's make sure the list of processes in that state aren't empty
		// Also, let's make sure the index we're trying to query isn't out of bounds
		if (processes.isEmpty() != true && this.getProcessCount(state) > index) {
			// Just in case it is out of bounds, let's catch the error
			try {
				// Return the first process in the list (at key/index 0)
				process = processes.get(index);
			}
			catch (IndexOutOfBoundsException exception) {
				// Only show if debugMode is on
				if (Simulation.debugMode) {
					System.out.println("Error getting process from the event manager at: State " + state + " and index " + index + ". With exception: " + exception);
				}
			}
		}

		return process;
	}

	// Public function to get the first process from the given state
	@Nullable public Process getProcess(String state) {
		return this.getProcessAtIndex(state, 0);
	}

	// Public function to get the single largest process in a given state's memory
	@Nullable public Process getLargestProcess(String state) {
		// Let's create a process to be returned
		Process process = null; // Process may be null. We may not get back a process

		// Get a list of processes in the given state
		List<Process> processes = this.getProcesses(state);

		// Let's make sure the list of processes in that state aren't empty
		if (processes.isEmpty() != true) {
			// Return the first process in the list (at key/index 0)
			process = Collections.max(processes);
		}

		return process;
	}

	// Public function to get the number of processes in a given state
	public int getProcessCount(String state) {
		// Let's grab a list of all of the current processes in the given state
		List<Process> processes = this.getProcesses(state);

		// Return the lists size
		return processes.size();
	}

	// Public function to change the state of a process given the Event
	public boolean changeProcessState(Event event) {
		// Let's first check if the destination state isn't full
		if (this.isStateFull(event.to) != true) {
			// Let's get the first process in the "from" location; "First-out"
			Process process = this.getProcess(event.from); // May be null

			// If we actually got back a process
			if (process != null) {
				// Let's remove the process from the original state
				// AND Let's now add the process to the destination state
				if (this.removeProcessFromState(process, event.from) && this.addProcess(process, event.to)) {
					// If we made it here, everything worked
					return true;
				}
			}
		}

		// If it got here, it didn't succeed
		return false;
	}

	// Public function to change the state of a given process to Hold from a given state
	public boolean changeProcessStateToHold(Process process, String fromState) {
		// Let's remove the process from the original state
		// AND Let's now add the process to the Hold state
		if (this.removeProcessFromState(process, fromState) && this.addProcess(process, "Hold")) {
			// If we made it here, everything worked
			return true;
		}

		// If it got here, it didn't succeed
		return false;
	}

	// Public function to get a count of the largest filled (most amount of processes) state in the event manager
	public int getMostFilledStateCount() {
		// Let's keep count
		int mostProcesses = 0;

		// Let's get all of the keys in the map
		Set<String> keys = this.systemStates.keySet();

		// Loop through each key
		for (String state : keys) {
			// Let's get the number of processes in that state
			int i = this.getProcessCount(state);
			
			// If its larger than our current max
			if (i > mostProcesses) {
				// Set the value
				mostProcesses = i;
			}
		}

		// When we're done, let's return the value
		return mostProcesses;
	}
}
