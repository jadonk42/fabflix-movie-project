import os

def parseLogFiles():
  file_index = 0
  nanoSecondsToMillSeconds = 1000000

  directory = "log"
  for filename in os.listdir(directory):
    totalSamples = 0
    search_time_total = 0
    database_time_total = 0
    file_index += 1
    print("Log File #" + str(file_index) + " Results:")

    f = os.path.join(directory, filename)
    
    with open(f) as file:
      for line in file:
        totalSamples += 1
        parseLine = line.split(" ")
        search_time = float(parseLine[0])
        search_time_total += search_time
        database_time = float(parseLine[1].strip())
        database_time_total += database_time


    search_time_average = search_time_total / totalSamples
    database_time_average = database_time_total / totalSamples

    search_time_average /= nanoSecondsToMillSeconds
    database_time_average /= nanoSecondsToMillSeconds
      
    
    print("Average Search Servlet Time: " + str(search_time_average) + " ms")
    print("Average JDBC Time: " + str(database_time_average) + " ms")
    print("\n")



if __name__ == "__main__":
  parseLogFiles()
