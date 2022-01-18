package io.microlam.hsm;

import org.junit.Test;

import io.microlam.hsm.impl.BasicHsm;

public class BasicHsmTest {

	@Test
	public void test1() {
		Hsm<LetterSignal,MyContext> hsm = new BasicHsm<LetterSignal, MyContext>("Hsm", "s0")
			.stateBuilder()
			.name("s0")
			.entry((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s0-entry;");
			})
			.exit((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s0-exit;");
			})
			.inner((Event<LetterSignal> event, MyContext context) -> {
				switch(event.getSignal()) {
					case e:
						System.out.println("s0-e;");
						return "s211";
					default:
						break;
				}
				return null;
				}
			)
			.initialChild("s1")
			.build()
			.name("s1")
			.entry((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s1-entry;");
			})
			.exit((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s1-exit;");
			})
			.inner((Event<LetterSignal> event, MyContext context) -> {
				switch(event.getSignal()) {
					case a:
						System.out.println("s1-a;");
						return "s1";
					case b:
						System.out.println("s1-b;");
						return "s11";
					case c:
						System.out.println("s1-c;");
						return "s2";
					case d:
						System.out.println("s1-d;");
						return "s0";
					case f:
						System.out.println("s1-f;");
						return "s211";
					default:
						break;
				}
				return null;
			})
			.parent("s0")
			.initialChild("s11")
			.build()
			.name("s11")
			.entry((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s11-entry;");
			})
			.exit((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s11-exit;");
			})
			.inner((Event<LetterSignal> event, MyContext context) -> {
				switch(event.getSignal()) {
					case g:
						System.out.println("s11-g;");
						return "s211";
					case h:
						if (context.myFoo) {
							System.out.println("s11-h;");
							context.myFoo = false;
							return "s1";
						}
					default:
						break;
				}
				return null;
				}
			)
			.parent("s1")
			.build()
			.name("s2")
			.entry((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s2-entry;");
			})
			.exit((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s2-exit;");
			})
			.inner((Event<LetterSignal> event, MyContext context) -> {
				switch(event.getSignal()) {
					case c:
						System.out.println("s2-c;");
						return "s1";
					case f:
						System.out.println("s2-f;");
						return "s11";
					default:
						break;
				}
				return null;
				}
			)
			.parent("s0")
			.initialChild("s21")
			.build()
			.name("s21")
			.entry((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s21-entry;");
			})
			.exit((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s21-exit;");
			})
			.inner((Event<LetterSignal> event, MyContext context) -> {
				switch(event.getSignal()) {
					case b:
						System.out.println("s21-b;");
						return "s1";
					case h:
						if (! context.myFoo) {
							System.out.println("s21-h;");
							context.myFoo = true;
							return "s21";
						}
					default:
						break;
				}
				return null;
				}
			)
			.parent("s2")
			.initialChild("s211")
			.build()
			.name("s211")
			.entry((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s211-entry;");
			})
			.exit((Event<LetterSignal> event, MyContext context) -> {
				System.out.println("s211-exit;");
			})
			.inner((Event<LetterSignal> event, MyContext context) -> {
				switch(event.getSignal()) {
					case d:
						System.out.println("s211-d;");
						return "s21";
					case g:
						System.out.println("s211-g;");
						return "s0";
					default:
						break;
				}
				return null;
				}
			)
			.parent("s21")
			.buildAndStop();
		
		LetterSignal[] signals = new LetterSignal[] {LetterSignal.a, LetterSignal.e, LetterSignal.e, LetterSignal.a, LetterSignal.h, LetterSignal.h};
		MyContext myContext = new MyContext();
		State<LetterSignal, MyContext> state = hsm.initState(myContext);
		String result = null;
		for(LetterSignal sig: signals) {
			MyEvent myEvent = new MyEvent(sig);
			System.out.println("Signal: "+ sig);
			result = state.process(myEvent, myContext);
			state = hsm.resolveState(result);
		}
	}
	
	enum LetterSignal {
		a, b, c, d, e, f, g, h;
	}

	class MyEvent implements Event<LetterSignal> {
		
		protected LetterSignal letterSignal;
		
		public MyEvent(LetterSignal letterSignal) {
			this.letterSignal = letterSignal;
		}

		@Override
		public LetterSignal getSignal() {
			return letterSignal;
		}
		
	}
	
	class MyContext implements Context {
		
		public boolean myFoo = false;
	}
	
}
