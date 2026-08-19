#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum Gesture {
    Rock,
    Paper,
    Scissors,
    Lizard,
    Spock,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum Outcome {
    PlayerOne,
    PlayerTwo,
    Draw,
}

pub fn resolve(a: Gesture, b: Gesture) -> Outcome {
    use Gesture::*;
    if a == b {
        return Outcome::Draw;
    }
    let wins = matches!(
        (a, b),
        (Rock, Scissors)
            | (Rock, Lizard)
            | (Paper, Rock)
            | (Paper, Spock)
            | (Scissors, Paper)
            | (Scissors, Lizard)
            | (Lizard, Spock)
            | (Lizard, Paper)
            | (Spock, Scissors)
            | (Spock, Rock)
    );
    if wins {
        Outcome::PlayerOne
    } else {
        Outcome::PlayerTwo
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn classic_rules() {
        assert_eq!(
            resolve(Gesture::Rock, Gesture::Scissors),
            Outcome::PlayerOne
        );
        assert_eq!(resolve(Gesture::Paper, Gesture::Rock), Outcome::PlayerOne);
        assert_eq!(
            resolve(Gesture::Scissors, Gesture::Paper),
            Outcome::PlayerOne
        );
    }

    #[test]
    fn extended_rules() {
        assert_eq!(
            resolve(Gesture::Lizard, Gesture::Spock),
            Outcome::PlayerOne
        );
        assert_eq!(resolve(Gesture::Spock, Gesture::Rock), Outcome::PlayerOne);
    }
}
