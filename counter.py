count = 0

print("Counter App")

while True:
    choice = input("Enter (+) to increment, (-) to decrement, (q) to quit: ")

    if choice == "+":
        count += 1
    elif choice == "-":
        count -= 1
    elif choice == "q":
        break
    else:
        print("Invalid input")
    
    print("Count:", count)
