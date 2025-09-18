import requests

food = input("Enter a dish name: ")
url = f"https://www.themealdb.com/api/json/v1/1/search.php?s={food}"

response = requests.get(url).json()

if response["meals"]:
    meal = response["meals"][0]
    print("Recipe:", meal["strMeal"])
    print("Instructions:", meal["strInstructions"][:200], "...")
else:
    print("Recipe not found")
