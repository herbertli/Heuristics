import math
from ortools.constraint_solver import pywrapcp
from ortools.constraint_solver import routing_enums_pb2
import numpy as np
from sklearn.cluster import KMeans

"""
To make the vehicle return, add constraint for time fo death, 

"""

class Test(object):

  def __init__(self):
    self.MAX_TIME = 10000
    # self.END_DEPOT_IND = None
    self.START_PATIENT_IND = None
    self.AM_CAPACITY = 4

  def create_data_model(self, patients):

    k_means = KMeans(n_clusters=1).fit([[x[0], x[1]] for x in patients])
    hospital_loc = [(int(x[0]), int(x[1])) for x in k_means.cluster_centers_]

    # (x, y, time_window_start, time_window_end)
    depots = []
    for p in patients:
      for h in hospital_loc:
        depots.append([h[0], h[1], 0, p[2]])
    # add end depot for arbitrary end locations
    # self.END_DEPOT_IND = len(depots)
    # depots.append([0, 0, self.MAX_TIME - 1, self.MAX_TIME])
    self.START_PATIENT_IND = len(depots)

    data = {}

    # time windows for depots
    data['time_windows'] = [(d[2], d[3]) for d in depots]
    # time windows for patients
    data['time_windows'].extend([(0, t[2]) for t in patients])
    print(data['time_windows'])

    # capacity for regular depots (negative means they reduce carried load)
    data['demands'] = [-self.AM_CAPACITY] * len(depots)
    # data['demands'] = [-self.AM_CAPACITY] * (len(depots) - 1)]
    # capacity for end node
    # data['demands'].append(4)
    # capacities for patients (positive means they increase carried load)
    data['demands'].extend([1] * len(patients))
    print(data['demands'])

    # death for regular depots
    data['death_times'] = [-self.MAX_TIME] * len(depots)
    # death for patients
    data['death_times'].extend([t[2] for t in patients])
    print(data['death_times'])

    # depot locations
    data['locations'] = [(h[0], h[1]) for h in depots]
    # patient locations
    data['locations'].extend(patients)
    print(data['locations'])

    assert len(data['locations']) == len(data['demands']), "Length Mismatch"
    assert len(data['death_times']) == len(data['demands']), "Length Mismatch"
    assert len(data['time_windows']) == len(data['demands']), "Length Mismatch"
    data['num_locations'] = len(data['locations'])

    # unload / load time
    data['time_per_demand_unit'] = 1
    
    data['vehicle_capacity'] = self.START_PATIENT_IND
    data['start_locations'] = [0]
    # data['end_locations'] = [self.END_DEPOT_IND] * len(data['start_locations'])
    data['end_locations'] = data['start_locations']
    data['num_vehicles'] = len(data['start_locations'])

    return data

  #######################
  # Problem Constraints #
  #######################

  def man_distance(self, position_1, position_2):
    lat1 = position_1[0]
    lat2 = position_2[0]
    lon1 = position_1[1]
    lon2 = position_2[1]
    return abs(lat1 - lat2) + abs(lon1 - lon2) 

  def create_distance_evaluator(self, data):
    """Creates callback to return distance between points."""
    _distances = {}
    # precompute distance between location to have distance callback in O(1)
    for from_node in range(data["num_locations"]):
      _distances[from_node] = {}
      for to_node in range(data["num_locations"]):
        _distances[from_node][to_node] = self.man_distance(data["locations"][from_node], data["locations"][to_node])
        # if from_node == self.END_DEPOT_IND or to_node == self.END_DEPOT_IND:
        #   _distances[from_node][to_node] = 0
        # else:
        #   _distances[from_node][to_node] = (
        #       self.man_distance(data["locations"][from_node],
        #                         data["locations"][to_node]))

    def distance_evaluator(from_node, to_node):
      """Returns the manhattan distance between the two nodes"""
      return _distances[from_node][to_node]

    return distance_evaluator

  def create_demand_evaluator_dwhl(self, data):
    """Creates callback to get demands at each location."""
    _demands = data["demands"]

    def demand_evaluator(from_node, to_node):
      """Returns the demand of the current node"""
      del to_node
      return _demands[from_node]

    # print('Demand_{}: {}'.format(ind, _demands))
    return demand_evaluator

  def add_capacity_constraints(self, routing, data, demand_evaluator):
    """Adds capacity constraint"""
    capacity = 'Capacity'
    _vehicle_capacity = data["vehicle_capacity"]
    routing.AddDimension(
        demand_evaluator,
        _vehicle_capacity, # Null slack
        _vehicle_capacity,
        True,  # start cumul to zero
        capacity)

    # Add Slack for reseting to zero unload depot nodes.
    # e.g. vehicle with load 10/15 arrives at node 1 (depot unload)
    # so we have CumulVar = 10(current load) + -15(unload) + 5(slack) = 0.
    capacity_dimension = routing.GetDimensionOrDie(capacity)
    # print('CapacityDimension: {}'.format(capacity_dimension))
    # for node_index in range(self.END_DEPOT_IND):
    for node_index in range(self.START_PATIENT_IND):
      index = routing.NodeToIndex(node_index)
      capacity_dimension.SlackVar(index).SetRange(0, _vehicle_capacity)
    # for node_index in range(self.END_DEPOT_IND, len(data['locations'])):
    for node_index in range(self.START_PATIENT_IND, len(data['locations'])):
      index = routing.NodeToIndex(node_index)
      capacity_dimension.SlackVar(index).SetRange(0, 0)

  def add_disjunction(self, routing, data):
    dodisjoint = True
    if dodisjoint:
      # for node_index in range(self.END_DEPOT_IND):
      for node_index in range(self.START_PATIENT_IND):
        # print('Depot NodeIndex: {}'.format(node_index))
        routing.AddDisjunction([node_index], 0)
      penalty = 100000
      for node_index in range(self.START_PATIENT_IND, data["num_locations"]):
        # print('Location NodeIndex: {}'.format(node_index))
        routing.AddDisjunction([node_index], penalty)

  def create_death_evaluator(self, data):
    def death_evaluator(from_node, to_node):
      return data["death_times"][from_node]
      # if data["death_times"][from_node] <= data["death_times"][to_node]:
      #   return 0 
      # else:
      #   return data["death_times"][to_node] - data["death_times"][from_node]
    
    return death_evaluator
  
  def add_death_constraints(self, routing, data, death_evaluator):
    death = 'Death'
    horizon = self.MAX_TIME # total
    routing.AddDimension(
        death_evaluator,
        horizon,  # allow waiting time
        horizon,  # maximum time per vehicle
        False,  # don't force start cumul to zero since we are giving death_time to start nodes
        death)
    death_dimension = routing.GetDimensionOrDie(death)
    # and "copy" the slack var in the solution object (aka Assignment) to print it
    # for node_index in range(self.END_DEPOT_IND):
    for node_index in range(self.START_PATIENT_IND):
      index = routing.NodeToIndex(node_index)
      death_dimension.SlackVar(index).SetRange(0, self.MAX_TIME)
      # death_dimension.SlackVar(index).SetRange(0, self.MAX_TIME)
    for node_index in range(self.START_PATIENT_IND, len(data['locations'])):
      index = routing.NodeToIndex(node_index)
      death_dimension.SlackVar(index).SetRange(0, 0)

  def create_time_evaluator(self, data):
    """Creates callback to get total times between locations."""
    def service_time(data, node):
      """Gets the service time for the specified location."""
      if data["demands"][node] < 0:
        return 0
      return data["demands"][node] * data["time_per_demand_unit"]

    def travel_time(data, from_node, to_node):
      """Gets the travel times between two locations."""
      if from_node == to_node:
        travel_time = 0
      else:
        travel_time = self.man_distance(data["locations"][from_node], data["locations"][to_node])
      return travel_time

    _total_time = {}
    # precompute total time to have time callback in O(1)
    for from_node in range(data["num_locations"]):
      _total_time[from_node] = {}
      for to_node in range(data["num_locations"]):
        if from_node == to_node:
          _total_time[from_node][to_node] = 0
        else:
          _total_time[from_node][to_node] = int(
              service_time(data, from_node) +
              travel_time(data, from_node, to_node))

    def time_evaluator(from_node, to_node):
      """Returns the total time between the two nodes"""
      return _total_time[from_node][to_node]

    return time_evaluator

  def add_time_window_constraints(self, routing, data, time_evaluator):
    """Add Time windows constraint"""
    time = 'Time'
    horizon = self.MAX_TIME # total
    routing.AddDimension(
        time_evaluator,
        horizon,  # allow waiting time
        horizon,  # maximum time per vehicle
        False,  # don't force start cumul to zero since we are giving TW to start nodes
        time)
    time_dimension = routing.GetDimensionOrDie(time)
    # Add time window constraints for each location except depot
    # and "copy" the slack var in the solution object (aka Assignment) to print it
    for location_idx, time_window in enumerate(data["time_windows"]):
      # if location_idx == self.END_DEPOT_IND:
        # continue
      index = routing.NodeToIndex(location_idx)
      time_dimension.CumulVar(index).SetRange(time_window[0], time_window[1])
      routing.AddToAssignment(time_dimension.SlackVar(index))
    # Add time window constraints for each vehicle start node
    # and "copy" the slack var in the solution object (aka Assignment) to print it
    for vehicle_id in range(data["num_vehicles"]):
      index = routing.Start(vehicle_id)
      end_index = routing.End(vehicle_id)
      time_dimension.CumulVar(index).SetRange(data["time_windows"][0][0], data["time_windows"][0][1])
      routing.AddToAssignment(time_dimension.SlackVar(index))
      # Warning: Slack var is not defined for vehicle's end node
      #routing.AddToAssignment(time_dimension.SlackVar(self.routing.End(vehicle_id)))

  ###########
  # Printer #
  ###########
  def print_solution(self, data, routing, assignment):
    """Prints assignment on console"""
    print('---------------------------')
    print('Objective: {}'.format(assignment.ObjectiveValue()))
    total_distance = 0
    total_load = 0
    total_time = 0
    capacity_dimension = routing.GetDimensionOrDie('Capacity')
    time_dimension = routing.GetDimensionOrDie('Time')
    death_dimension = routing.GetDimensionOrDie('Death')
    dropped = []
    for order in range(0, routing.nodes()):
      index = routing.NodeToIndex(order)
      if index != -1 and assignment.Value(routing.NextVar(index)) == index:
        dropped.append(order)
    print('dropped orders: {}'.format(dropped))

    for vehicle_id in range(data["num_vehicles"]):
      index = routing.Start(vehicle_id)
      plan_output = 'Route for vehicle {}:\n'.format(vehicle_id)
      distance = 0
      while not routing.IsEnd(index):
        load_var = capacity_dimension.CumulVar(index)
        death_var = death_dimension.CumulVar(index)
        time_var = time_dimension.CumulVar(index)
        plan_output += ' {0} Load({1}) Time({2},{3}) Death({4}) ->'.format(
            routing.IndexToNode(index),
            assignment.Value(load_var),
            assignment.Min(time_var),
            assignment.Max(time_var),
            assignment.Value(death_var))
        previous_index = index
        index = assignment.Value(routing.NextVar(index))
        distance += routing.GetArcCostForVehicle(previous_index, index,
                                                vehicle_id)
      load_var = capacity_dimension.CumulVar(index)
      time_var = time_dimension.CumulVar(index)
      plan_output += ' {0} Load({1}) Time({2},{3})\n'.format(
          routing.IndexToNode(index),
          assignment.Value(load_var),
          assignment.Min(time_var),
          assignment.Max(time_var))
      plan_output += 'Distance of the route: {}m\n'.format(distance)
      plan_output += 'Load of the route: {}\n'.format(assignment.Value(load_var))
      plan_output += 'Time of the route: {}\n'.format(assignment.Value(time_var))
      print(plan_output)
      total_distance += distance
      total_load += assignment.Value(load_var)
      total_time += assignment.Value(time_var)
    print('Total Distance of all routes: {}m'.format(total_distance))
    print('Total Load of all routes: {}'.format(total_load))
    print('Total Time of all routes: {}min'.format(total_time))

  ########
  # Main #
  ########
  def main(self, patients):
    """Entry point of the program"""
    # Instantiate the data problem.
    data = self.create_data_model(patients)

    # Create Routing Model
    routing = pywrapcp.RoutingModel(
        data["num_locations"],
        data["num_vehicles"],
        data["start_locations"],
        data["end_locations"])
    # Define weight of each edge
    distance_evaluator = self.create_distance_evaluator(data)
    routing.SetArcCostEvaluatorOfAllVehicles(distance_evaluator)
    # Add Capacity constraint
    demand_evaluator = self.create_demand_evaluator_dwhl(data)
    self.add_capacity_constraints(routing, data, demand_evaluator)
    print("Added Capacity Constraints")

    # Add Death time constraint
    death_evaluator = self.create_death_evaluator(data)
    self.add_death_constraints(routing, data, death_evaluator)
    print("Added Death Time Constraints")

    # allow incomplete solutions
    self.add_disjunction(routing, data)
    print("Added Disjunction")

    # Add Time Window constraint
    time_evaluator = self.create_time_evaluator(data)
    self.add_time_window_constraints(routing, data, time_evaluator)
    print("Added Time Window Constraints")

    # Setting first solution heuristic (cheapest addition).
    search_parameters = pywrapcp.RoutingModel.DefaultSearchParameters()
    search_parameters.first_solution_strategy = (
        routing_enums_pb2.FirstSolutionStrategy.PATH_CHEAPEST_ARC)  # pylint: disable=no-member
    # Solve the problem.
    assignment = routing.SolveWithParameters(search_parameters)
    self.print_solution(data, routing, assignment)

if __name__ == '__main__':
  patients = [
    (36,52,116),
    # (36,52,116),
    # (36,52,116),
    # (36,52,116),
    # (36,52,116),
    (15,12,26),
    (17,29,5000),
    # (18,23,43),
    # (26,14,56),
    # (12,14,53),
    (68,75,2000),
    # (56,72,46),
    # (76,86,25),
    (70,85,2000),
    # (70,85,69)
  ]
  t = Test()
  t.main(patients)
