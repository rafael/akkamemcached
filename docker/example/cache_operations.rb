#!/usr/bin/env ruby 

if ENV['USE_DOCKER']
  `dockerize -wait tcp://memcached:11211 -timeout 120s`
end

require 'dalli'

# Dummy method to test CAS. Try to set a key using the CAS method,
# keep retrying while it doesn't succeeds.
def bump_counter(key, client)
  new_value = nil
  while true do
    counter = client.get(key)
    raise "Error, trying to bump key that doesn't exists" unless counter
    new_value = client.cas(key) { |value| value + 1 }
    if new_value
      break
    else
      # You may comment this line if want to see in the output the collissions while they occur
      #puts "Oops collision, need to try again"
    end
  end
  new_value
end

def client
  port = ENV['MEMCACHED_PORT'] || 11211
  host = ENV['MEMCACHED_HOST'] || "memcached"
  Dalli::Client.new("#{host}:#{port}")
end

def random_test
  puts "Setting key: a value: test"
  client.set("a", "test")
  value = client.get("a")
  puts "Get key: a value was #{value}"
  raise "Error, set a key and the value was not there" unless value == "test"
  response_delete = client.delete("a")
  puts "Delete key: a, response was: #{response_delete}"
  raise "Key was not deleted" if client.get("a") 
end

puts "Starting test, setting, fetching and deleting a value"
random_test # => nil
puts "First test finished"
puts "Starting test for CAS"
puts "Going to start 4 threads. Each threads will call incr 10 time. By the end of the loop, the counter should have the value 40 if it's being atomic."
client.set("incr", 0) # => 1
# Set the threads going
threads = []
4.times.each { |thing|
  thread = Thread.new(thing) do 
    thread_client = client
    10.times.each do
      bump_counter("incr", thread_client)
    end
  end
  threads << thread
}

# Wait for the threads to finish
threads.each { |thread|
  thread.join
}

final_count = client.get('incr')
raise "Error: CAS didn't guarantee isolation: It should be eq to 40, got #{final_count}" if final_count != 40
puts "The final value of the counter: incr is #{client.get('incr')}"
