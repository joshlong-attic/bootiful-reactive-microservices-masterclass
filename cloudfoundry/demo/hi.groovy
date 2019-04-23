@RestController 
class GreetingsRestController {

	@GetMapping("/hi/{name}")
	def greet (@PathVariable String name){
	 [ greeting :  "Hello " + name + "!"  ]
	}
}

