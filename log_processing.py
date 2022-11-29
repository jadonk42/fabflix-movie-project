import os
import sys

def parseLogFile(file, file_index):
  nanoSecondsToMillSeconds = 1000000
  totalSamples = 0
  search_time_total = 0
  database_time_total = 0
  print("Log File #" + str(file_index) + " Results:")

  with open(file) as f:
    for line in f:
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


def main():
  if len(sys.argv) < 3:
    print("No Log file to process and no case number specified")
    sys.exit(0)

  if len(sys.argv) > 3:
    print("Too many files. Only Specify one Log file and a case number")
    sys.exit(0)

  file = sys.argv[1]
  test_case = sys.argv[2]
  fileExtension = os.path.splitext(file)[1]
  if fileExtension.lower() != ".txt":
    print("Invalid file type. File must be a text file")
    sys.exit(0)

  if not test_case.isdigit():
    print("Not a valid number. Must enter a positive integer")
    sys.exit(0)

  index_case = int(test_case)
  parseLogFile(file, index_case)



if __name__ == "__main__":
  main()
