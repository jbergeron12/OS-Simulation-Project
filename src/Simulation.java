/********************************/
// Operating Systems Simulation //
//   Created by Trevor Suarez   //
/********************************/

// Imports (libraries and utilities)
import java.util.List;
import java.util.Random;

// External imports
import com.google.common.collect.*;

// Simulation main engine class
public class Simulation {
	// Declare final variables (constants)
	public static final int MAX_MEMORY = 2048; // Total available user memory
	public static final int MAX_EVENTS = 500; // Maximum number of events to be fired before quitting

	private static final String[] STATE_NAMES = {"Hold", "Ready", "Run", "Suspend", "Blocked", "Done"}; // The names of each possible state

	private static final int[] INITIAL_JOB_STATES = {1, 3, 4}; // The initially active job states (correspond with the state names key/index)
	private static final int INITIAL_JOB_SIZE = 320; // The amount of memory that each initial ACTIVE job has
	private static final int INITIAL_JOB_TIME = 6; // The CPU time requirement of each initially ACTIVE job
	private static final int INITIAL_NUM_HELD = 10; // The number of initially inactive/held jobs

	// Program wide objects
	public static Random random;

	// Class wide objects
	private static EventManager states;
	private static MemoryManager memory;
	private static List<Event> events;
	private static boolean systemRunning;
	private static int totalEventCount;

	// Constructor
	private static void run() {
		// Let's create/start our event manager
		states = new EventManager();

		// Let's create our memory manager
		memory = new MemoryManager();

		// Let's fill our event array list with our randomized events
		buildEventsList();

		// Let's initialize the system with our initial conditions
		initialConditions();

		// Ok. Everything's set up, so let's run the system
		startSystem();
	}

	// Private function to setup the initial conditions
	private static void initialConditions() {
		// Let's create our initially active processes
		for (int state : INITIAL_JOB_STATES) {
			// Create the process
			Process job = new Process(INITIAL_JOB_SIZE, INITIAL_JOB_TIME);

			// Add the process to the event manager's map
			states.addProcess(job, STATE_NAMES[state]);
			System.out.println("Process created at state: \"" + STATE_NAMES[state] + "\" with ID: " + job.getId() + ", Size: " + job.getSize() + "k, and Time: " + job.getTime());

			// Add the process to the system's memory
			memory.addProcess(job);
			System.out.println("Process " + job.getId() + " added to memory with size " + job.getSize() + "k");
		}

		// Now, let's create our initially inactive/held jobs
		for (int i = 0; i < INITIAL_NUM_HELD; i++) {
			// Create the process
			Process job = new Process();

			// Add the process to the event manager's map
			states.addProcess(job, "Hold");
			System.out.println("Process created at state: \"Hold\" with ID: " + job.getId() + ", Size: " + job.getSize() + "k, and Time: " + job.getTime());
		}
	}

	// Private function to build the event list
	private static void buildEventsList() {
		// First of all, let's instanciate an array list
		events = Lists.newArrayList();

		// Let's add our events to the array list
		events.add(new Event("Hold", "Ready")); // Event from and to
		events.add(new Event("Ready", "Run"));
		events.add(new Event("Run", "Blocked"));
		events.add(new Event("Blocked", "Ready"));
		events.add(new Event("Run", "Suspend")); // User
		events.add(new Event("Run", "Suspend")); // Timer/System
		events.add(new Event("Blocked", "Done")); // System killed
		events.add(new Event("Suspend", "Done")); // User killed
		events.add(new Event("Suspend", "Ready")); // User
		events.add(new Event("Suspend", "Ready")); // Timer/System
		events.add(new Event("Run", "Done"));
		events.add(new Event("Ready", "Hold"));
	}

	// Private function to generate a random event from the events list
	private static Event generateRandomEvent() {
		// Create the number's maximum range
		int randMax = events.size();

		// Generate a random int within the constraints
		int n = random.nextInt(randMax);

		// Let's get the event at that random position n
		Event generatedEvent = events.get(n);
		System.out.println(generatedEvent.toString());

		// Return the randomly generated event
		return generatedEvent;
	}

	// Private function to check if the system has finished its job
	private static boolean checkFinished() {
		if (totalEventCount == 500) {
			return true;
		}

		// TODO: Create other finish conditions

		// If it got here, the system hasn't finished yet
		return false;
	}

	// Private function to actually start the system
	private static void startSystem() {
		// Mark the system as running
		systemRunning = true;

		// While the system is still running
		while (systemRunning) {
			// Let's generate a random event
			generateRandomEvent();

			// Let's increment the total number of events that have been generated
			totalEventCount++;

			// Let's check to see if the system has finished its job
			if (checkFinished()) {
				systemRunning = false;
			}
		}
	}

	// Main function
	public static void main(String[] args) {
		// Instanciate program wide objects
		random = new Random();

		// Begin the simulation
		run();
	}

} // End Simulation class
