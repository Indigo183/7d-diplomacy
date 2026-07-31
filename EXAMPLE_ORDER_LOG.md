# 7D Diplomacy - Example Order Log

-# *This document refers to the attached images in its explanation: the first image shows **T0**, the second **T1**, and the third **T2**. The images are a **single board state**, and shows the **final board state** of a game in which Cato (blue) beats Pompey (red).*

## Diagram Notations

Before we begin, we must establish the meaning behind the various notations on the map:
 - Each image represents an individual timeplane.
 - The origin board on each timeplane is represented by a yellow square outline.
 - Each board is made up of four provinces:
   - Cato (top left, abbreviated to CAT, home supply centre for Cato the player)
   - Caesar (top right, abbreviated to CAE)
   - Brutus (bottom left, abbreviated to BRU)
   - Pompey (bottom right, abbreviated to POM, home supply centre for Pompey the player)
 - Each board has a small square in the bottom right corner, where a filled-in square means that that board is dead and an empty square means that that board is alive.
 - Cato's owned supply centres are coloured dark blue and their units are coloured light blue.
 - Pompey's owned supply centres are coloured red and their units are coloured orange.
 - A move is denoted by a black arrow, with a small green arrow extending from the centre representing the move's temporal flare. A failed move is the same but red.
 - A support is denoted by a dotted line, to the centre of either the move or the unit being supported.
 - A build is denoted by a green outline around the built unit.
 - A disband is denoted by a green line striking through the disbanding unit.
 - When a move or support cuts across timeplanes, there is a dot on the lower timeplane and a cross on the upper timeplane in the same location to show the correlation.
 - When a board has a child on the same timeplane, it is shown with a large green arrow connecting them.
 - When a board has a child on a higher timeplane, it is shown with a small green arrow leading away from the parent and a separate small green arrow leading towards the child.

*I will admit that these are not the nicest diagrams, nor is the notation the most thought out, but in my defense, these diagrams are from the 8th of February.*

## Order Notations

The order format I have used in this document is as follows:
```
[timeplane]:
...
* [board]<, [timeplane]> [province]< S <[board]<, [timeplane]>> [province]> - [board]<, [timeplane]> [province] <i [flare]>
...
```
where `[content]` marks any literal to be replaced with whatever it represents, and `<content>` marks any optional content. For example, a full order set might look like:
```
T0:
* (0, T0) Cat S (0, T0) Pom - (0, T0) Cae
* (0, T0) Pom - (0, T0) Cae i 0
```
(I also include brackets for clarity when I specify both the location and timeplane.) However, lots of this is unnecessary boiler-plate that can be stripped away while preserving meaning:
```
T0:
* 0 Cat S Pom - Cae
* 0 Pom - Cae i 0
```
If there is ever any ambiguity, it is best practice to specify more detail until the ambiguity is removed.

## TURN 1

Cato:
```
T0:
* 0 Cat - Bru i 2
```
Pompey:
```
T0:
* 0 Pom - Bru i 1
```

Notice how both units move to the same province but *do not bounce*; this is because they move in *different temporal directions*, and in all other directions, they hold. Also, each asterisk is simply marking a list entry, not part of the order itself.

*(RETREATS: none)*

*(ADJUSTMENTS: none)*

## TURN 2

Cato:
```
T0:
* i Cat - 0 Cat i 2 (failed)
* -1 Bru - Pom i 1
```
Pompey:
```
T0:
* i Bru - 0 Cat i 2 (failed)
* -1 Pom - 0 Pom i 1
```

This turn, Cato bounces Pompey out of CAT on the origin board, and Pompey tries the same but it backfires, as Cato is free to walk into POM. Notice how no child boards are created from board **i**, as all units bounce, while the origin board is duplicated into board **i** on **T1** due to Pompey's cross-board move from board **-1**.

*(RETREATS: none)*

*(ADJUSTMENTS: **(i-1, T0)**)*

## TURN 2 ADJUSTMENTS

Cato:
```
T0:
* Build Cat
```

## TURN 3

Cato:
```
T0:
* i-1 Cat - i Cae i 3
* i-1 Pom - i Pom i 3

T1:
* i Cat S (i-1, T0) Cat - (i, T0) Cae
```
Pompey:
```
T1:
* i Pom - Cae i 3
* i Bru S Pom - Cae
```

Here's where things get interesting. In a bid to control **(0, T1)**, both parties support seemingly useless moves, in the hopes of their preferred board having more strength than the other's. However, while Pompey musters up a board strength of 2, Cato gains extra strength from a cross-board support, bringing the strength of **(i, T0) i 3** up to 3. **(0, T1)**, therefore, is a child of **(i, T0)**, while **(i, T1)**'s child is forced up into **T2**.

*(RETREATS: none)*

*(ADJUSTMENTS: **(0, T1)**, **(0, T2)**)*

## TURN 3 ADJUSTMENTS

Cato:
```
T1:
* Disband 0 Cae
```
Pompey:
```
T1:
* Disband 0 Bru

T2:
* Disband 0 Cae
```

## TURN 4

Cato:
```
T1:
* 0 Cat - Bru i 3
* 0 Pom - Cae i 0

T2:
* 0 Cat - Cae i 2
```
Pompey:
```
T2:
* 0 Bru - Cae i 0 (failed)
```

Here, Cato does their best to win this turn by creating many boards where they own both CAT and POM. This succeeds. Pompey, in a last-ditch effort, tries to guess in which temporal direction **(0, T2)** CAT is moving, and fails. However, they still get a desirable board, as CAT leaving the board results in a child board where Pompey still controls one centre.
Cato wins here because they control 4/4 active copies of CAT and 3/4 active copies of POM.
