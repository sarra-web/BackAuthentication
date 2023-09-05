package com.bezkoder.spring.login.controllers;

import com.bezkoder.spring.login.exception.ResourceNotFoundException;
import com.bezkoder.spring.login.models.ERole;
import com.bezkoder.spring.login.models.Role;
import com.bezkoder.spring.login.models.UpdateRequest;
import com.bezkoder.spring.login.models.User;
import com.bezkoder.spring.login.payload.request.LoginRequest;
import com.bezkoder.spring.login.payload.request.SignupRequest;
import com.bezkoder.spring.login.payload.response.MessageResponse;
import com.bezkoder.spring.login.payload.response.UserInfoResponse;
import com.bezkoder.spring.login.repository.RoleRepository;
import com.bezkoder.spring.login.repository.UserRepository;
import com.bezkoder.spring.login.security.jwt.JwtUtils;
import com.bezkoder.spring.login.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.OK;

//@CrossOrigin(origins = "*", maxAge = 3600)
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @GetMapping("/AllUsers")
  public ResponseEntity<List<User>> getAllUsers( ) {
    List<User> users = userRepository.findAll();
    return new ResponseEntity<>(users, HttpStatus.OK);
  }


  @GetMapping("/users")
  public ResponseEntity<Map<String, Object>> getAllUsers(
          @RequestParam(defaultValue = "") String name,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "3") int size
  ) {

    try {
      List<User> users = new ArrayList<User>();
      Pageable paging = PageRequest.of(page, size);

      Page<User> pageTuts;

      pageTuts = userRepository.findByUsernameContaining(name, paging);

      users = pageTuts.getContent();

      Map<String, Object> response = new HashMap<>();
      response.put("connectors", users);
      response.put("currentPage", pageTuts.getNumber());
      response.put("totalItems", pageTuts.getTotalElements());
      response.put("totalPages", pageTuts.getTotalPages());
      return new ResponseEntity<>(response, OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  @GetMapping("/AllRoles")
  public ResponseEntity<List<Role>> getAllRoles( ) {
    List<Role> users = roleRepository.findAll();
    return new ResponseEntity<>(users, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable(value = "id") Long id) {
   User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found User with id = " + id));

    return new ResponseEntity<>(user, HttpStatus.OK);
  }
  @GetMapping("/findByName/{name}")
  public ResponseEntity<User> getUserByName(@PathVariable(value = "name") String name) {
    User user = userRepository.findByUsername(name)
            .orElseThrow(() -> new ResourceNotFoundException("Not found User with id = " + name));

    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
        .body(new UserInfoResponse(userDetails.getId(),
                                   userDetails.getUsername(),
                                   userDetails.getEmail(),
                                   roles));
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(),
                         signUpRequest.getEmail(),
                         encoder.encode(signUpRequest.getPassword()));

    Set<String> strRoles = signUpRequest.getRole();
    System.out.println("strRoles"+strRoles);
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
        case "ROLE_ADMIN":
          Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(adminRole);

          break;
        case "mod":
          Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(modRole);

          break;
        default:
          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(userRole);
        }
      });
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser() {
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(new MessageResponse("You've been signed out!"));
  }


  @PutMapping("/a")
  public ResponseEntity<?>  updateSquedulerDAO( @Valid @RequestBody UpdateRequest userRequest) {
    /*if (userRepository.existsByUsername(userRequest.getUsername())) {

      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(userRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }*/

    // Create new user's account




    String password=userRequest.getPassword();;
    if(password.length()<=20){
      password=encoder.encode(password);
    }

      User user = new User(userRequest.getId(),userRequest.getUsername(),
              userRequest.getEmail(), password,userRequest.getUserToken());




    Set<String> strRoles = userRequest.getRoles();
    Set<Role> roles = new HashSet<>();

   if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
     roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        if (role.equals("ROLE_ADMIN")) {
          Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          System.out.println(roles);

          roles.add(adminRole);

        } else if (role.equals(ERole.ROLE_MODERATOR)) {
          Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

          roles.add(modRole);

        } else {
          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          System.out.println(roles);


         roles.add(userRole);

        }
      });
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User updated successfully!"));
    /* User user = userRepository.findById(userRequest.getId())
            .orElseThrow(() -> new ResourceNotFoundException("UserId not found"));

    user.setEmail(userRequest.getEmail());
    user.setPassword(userRequest.getPassword());
    user.setUsername(userRequest.getUsername());
    user.setRoles(userRequest.getRoles());

    return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);*/
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") long id) {
    userRepository.deleteById(id);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/all")
  public ResponseEntity<HttpStatus> deleteAllUsers() {
    userRepository.deleteAll();

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }


}
