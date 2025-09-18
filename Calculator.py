print("Simple Calculator")

while True:
    print("\nOptions: +  -  *  /  q(quit)")
    choice = input("Choose: ")

    if choice == "q":
        break

    a = float(input("Enter first number: "))
    b = float(input("Enter second number: "))

    if choice == "+":
        print("Result:", a + b)
    elif choice == "-":
        print("Result:", a - b)
    elif choice == "*":
        print("Result:", a * b)
    elif choice == "/":
        print("Result:", a / b if b != 0 else "Error! Divide by zero.")
    else:
        print("Invalid choice")
