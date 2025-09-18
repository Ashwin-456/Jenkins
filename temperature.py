print("Temperature Converter")

temp = float(input("Enter temperature: "))
unit = input("Convert to (C/F): ").upper()

if unit == "C":
    print("Celsius:", (temp - 32) * 5/9)
elif unit == "F":
    print("Fahrenheit:", (temp * 9/5) + 32)
else:
    print("Invalid choice")
